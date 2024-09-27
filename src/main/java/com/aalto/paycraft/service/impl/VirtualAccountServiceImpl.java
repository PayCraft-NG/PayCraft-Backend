package com.aalto.paycraft.service.impl;

import com.aalto.paycraft.dto.*;
import com.aalto.paycraft.dto.enums.Currency;
import com.aalto.paycraft.entity.Employer;
import com.aalto.paycraft.entity.VirtualAccount;
import com.aalto.paycraft.entity.WebhookData;
import com.aalto.paycraft.repository.EmployerRepository;
import com.aalto.paycraft.repository.VirtualAccountRepository;
import com.aalto.paycraft.repository.WebhookDataRepository;
import com.aalto.paycraft.service.IKoraPayService;
import com.aalto.paycraft.service.IVirtualAccountService;
import com.aalto.paycraft.service.JWTService;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.function.Function;

@Service
@RequiredArgsConstructor
@Slf4j
public class VirtualAccountServiceImpl implements IVirtualAccountService {
    private final VirtualAccountRepository virtualAccountRepository;
    private final WebhookDataRepository webhookDataRepository;

    private final IKoraPayService koraPayService;
    private final EmployerRepository employerRepository;
    private final JWTService jwtService;
    private final HttpServletRequest request;

    // Gets the AccessToken from the Request Sent
    private String EMPLOYER_ACCESS_TOKEN(){
        return request.getHeader("Authorization").substring(7);
    }

    // Get the ID of the employer making the request
    private Employer EMPLOYER(){
        verifyTokenExpiration(EMPLOYER_ACCESS_TOKEN());
        Claims claims = jwtService.extractClaims(EMPLOYER_ACCESS_TOKEN(), Function.identity());
        UUID employerId =  UUID.fromString((String) claims.get("userID"));

        // Get the Employer Details from the DB
        Employer employer = new Employer();
        Optional<Employer> optionalEmployer = employerRepository.findByEmployerId(employerId);
        if(optionalEmployer.isPresent()) employer = optionalEmployer.get();
        return employer;
    }

    /* Method to Verify Token Expiration */
    private void verifyTokenExpiration(String token) {
        if (jwtService.isTokenExpired(token)) {
            log.warn("Token has expired");
            throw new ExpiredJwtException(null, null, "Access Token has expired");
        }
    }

    @Override
    public DefaultApiResponse<VirtualAccountDTO> createVirtualAccount() {
        DefaultApiResponse<VirtualAccountDTO> response = new DefaultApiResponse<>();
        VirtualAccount virtualAccount;
        VirtualAccountDTO virtualAccountDTO;

        DefaultKoraResponse<VirtualAccountResponseDTO> responseBody = null;
        try {
            responseBody = koraPayService.createVirtualAccount(EMPLOYER());
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        if(responseBody.getMessage().equals("Virtual bank account created successfully")){
            VirtualAccountResponseDTO data = responseBody.getData();
            virtualAccount = VirtualAccount.builder()
                .accountNumber(data.getAccount_number())
                .accountReference(data.getAccount_reference())
                .koraAccountReference(data.getUnique_id())
                .bankCode(data.getBank_code())
                .bankName(data.getBank_name())
                .accountStatus(data.getAccount_status())
                .balance(BigDecimal.ZERO)
                .currency(Currency.valueOf(data.getCurrency()))
                .employer(EMPLOYER()).build();

            VirtualAccount savedVirtualAccount = virtualAccountRepository.save(virtualAccount);

            response.setStatusCode("00");
            response.setStatusMessage("Virtual bank account created successfully");
            virtualAccountDTO = VirtualAccountDTO.builder()
                    .virtualAccountId(String.valueOf(savedVirtualAccount.getAccountId()))
                    .accountNumber(savedVirtualAccount.getAccountNumber())
                    .bankCode(savedVirtualAccount.getBankCode())
                    .bankName(savedVirtualAccount.getBankName())
                    .accountStatus(savedVirtualAccount.getAccountStatus())
                    .balance(savedVirtualAccount.getBalance())
                    .currency(savedVirtualAccount.getCurrency())
                    .employerId(String.valueOf(EMPLOYER().getEmployerId())).build();
            response.setData(virtualAccountDTO);
        }else{
            response.setStatusCode("49");
            response.setStatusMessage("Unable to create virtual account");
            return response;
        }
        return response;
    }

    @Override
    public DefaultApiResponse<VirtualAccountTransactionDTO> getTransactionsOfVba(
            String startDate, String endDate, Integer page, Integer limit){
        DefaultApiResponse<VirtualAccountTransactionDTO> response = new DefaultApiResponse<>();

        VirtualAccount virtualAccount = new VirtualAccount();
        Optional<VirtualAccount> optionalVirtualAccount =
                virtualAccountRepository.findByEmployer_EmployerId(EMPLOYER().getEmployerId());
        if(optionalVirtualAccount.isPresent()) virtualAccount = optionalVirtualAccount.get();

        VirtualAccountTransactionDTO transactionDTO;

        DefaultKoraResponse<VBATransactionDTO> responseBody = null;
        try {
            responseBody = koraPayService.getTransactionOfVBA(
                    virtualAccount.getAccountNumber(), EMPLOYER(), startDate, endDate, page, limit);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        if(responseBody.getMessage().equals("Virtual bank account transactions retrieved successfully")){
            VBATransactionDTO data = responseBody.getData();

            List<VirtualAccountTransactionDTO.TransactionDTO> transactions = new ArrayList<>();

            for(VBATransactionDTO.TransactionDTO transaction : data.getTransactions()){
                VirtualAccountTransactionDTO.TransactionDTO t = VirtualAccountTransactionDTO.TransactionDTO.builder()
                        .payerAccountNumber(transaction.getPayer_bank_account().getAccount_number())
                        .payerAccountName(transaction.getPayer_bank_account().getAccount_name())
                        .payerBankName(transaction.getPayer_bank_account().getBankName())
                        .reference(transaction.getReference())
                        .description(transaction.getDescription())
                        .status(transaction.getStatus())
                        .amount(transaction.getAmount())
                        .fee(transaction.getFee())
                        .currency(transaction.getCurrency())
                        .build();
                transactions.add(t);
            }

            transactionDTO = VirtualAccountTransactionDTO.builder()
                    .totalPages(data.getPagination().getTotal_pages())
                    .transactions(transactions)
                    .build();

            response.setStatusCode("00");
            response.setStatusMessage("Virtual bank account created successfully");
            response.setData(transactionDTO);

        }else {
            response.setStatusCode("49");
            response.setStatusMessage(
                    "Unable to get Transactions for virtual account: " + virtualAccount.getAccountNumber());
            return response;
        }
        return response;
    }

    @Override
    public DefaultApiResponse<BankTransferDetailsDTO> processBankTransfer(BigDecimal amount){
        DefaultApiResponse<BankTransferDetailsDTO> response = new DefaultApiResponse<>();

        DefaultKoraResponse<BankTransferResponseDTO> responseBody;
        try {
            responseBody = koraPayService.initiateBankTransfer(amount, EMPLOYER());
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        if(responseBody.getMessage().equals("Bank transfer initiated successfully")){
            BankTransferResponseDTO data = responseBody.getData();

            BankTransferDetailsDTO detailsForTransfer = BankTransferDetailsDTO.builder()
                    .amount(BigDecimal.valueOf(data.getAmount()))
                    .amountExpected(BigDecimal.valueOf(data.getAmount_expected()))
                    .referenceNumber(data.getReference())
                    .paymentReference(data.getPayment_reference())
                    .accountName(data.getBank_account().getAccount_name())
                    .accountNumber(data.getBank_account().getAccount_number())
                    .bankName(data.getBank_account().getBank_name())
                    .expiryDate(String.valueOf(data.getBank_account().getExpiry_date_in_utc()))
                    .build();

            response.setStatusCode("00");
            response.setStatusMessage("Bank transfer created successfully");
            response.setData(detailsForTransfer);
        }else {
            response.setStatusCode("49");
            response.setStatusMessage("Bank transfer initialization failed");
            return response;
        }

        return response;
    }

    @Override
    public DefaultApiResponse<?> verifyBankTransfer(String referenceNumber){
        DefaultApiResponse<?> response = new DefaultApiResponse<>();

        VirtualAccount virtualAccount = new VirtualAccount();
        Optional<VirtualAccount> optionalVirtualAccount =
                virtualAccountRepository.findByEmployer_EmployerId(EMPLOYER().getEmployerId());
        if(optionalVirtualAccount.isPresent()) virtualAccount = optionalVirtualAccount.get();

        WebhookData webhookData = new WebhookData();
        Optional<WebhookData> webhookDataOptional = webhookDataRepository.findByReference(referenceNumber);
        if(webhookDataOptional.isPresent()){
            webhookData = webhookDataOptional.get();
        }
        if(webhookData.getEvent().equals("charge.success")){

            virtualAccount.setBalance(webhookData.getAmount());
            virtualAccountRepository.save(virtualAccount);
            response.setStatusCode("00");
            response.setStatusMessage("Bank transfer successfully");
        }else {
            response.setStatusCode("49");
            response.setStatusMessage("Bank transfer failed");
        }
        return response;
    }
}
