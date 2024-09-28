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

import static com.aalto.paycraft.constants.PayCraftConstant.REQUEST_SUCCESS;
import static com.aalto.paycraft.constants.PayCraftConstant.STATUS_400;

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

    // Extract the AccessToken from the incoming request
    private String EMPLOYER_ACCESS_TOKEN() {
        return request.getHeader("Authorization").substring(7);  // Remove "Bearer " prefix
    }

    // Get Employer details using the token in the request
    private Employer EMPLOYER() {
        verifyTokenExpiration(EMPLOYER_ACCESS_TOKEN());  // Check token validity
        Claims claims = jwtService.extractClaims(EMPLOYER_ACCESS_TOKEN(), Function.identity());
        UUID employerId = UUID.fromString((String) claims.get("userID"));

        // Fetch employer details from the repository
        Optional<Employer> optionalEmployer = employerRepository.findByEmployerId(employerId);
        if (optionalEmployer.isPresent()) {
            return optionalEmployer.get();
        }

        log.warn("Employer not found with ID: {}", employerId);  // Log if employer is not found
        return new Employer();  // Return empty employer object as fallback
    }

    // Check if the token has expired and throw an exception if so
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

        try {
            // Call external KoraPay service to create a virtual account
            DefaultKoraResponse<VirtualAccountResponseDTO> responseBody = koraPayService.createVirtualAccount(EMPLOYER());

            // Check if the account creation was successful
            if (responseBody.getMessage().equals("Virtual bank account created successfully")) {
                VirtualAccountResponseDTO data = responseBody.getData();

                // Build a VirtualAccount entity using the response data
                virtualAccount = VirtualAccount.builder()
                        .accountNumber(data.getAccount_number())
                        .accountReference(data.getAccount_reference())
                        .koraAccountReference(data.getUnique_id())
                        .bankCode(data.getBank_code())
                        .bankName(data.getBank_name())
                        .accountStatus(data.getAccount_status())
                        .balance(BigDecimal.ZERO)  // Initial balance is zero
                        .currency(Currency.valueOf(data.getCurrency()))
                        .employer(EMPLOYER())  // Link to employer
                        .build();

                // Save the new virtual account in the repository
                VirtualAccount savedVirtualAccount = virtualAccountRepository.save(virtualAccount);

                // Build response DTO with saved account details
                virtualAccountDTO = VirtualAccountDTO.builder()
                        .virtualAccountId(String.valueOf(savedVirtualAccount.getAccountId()))
                        .accountNumber(savedVirtualAccount.getAccountNumber())
                        .bankCode(savedVirtualAccount.getBankCode())
                        .bankName(savedVirtualAccount.getBankName())
                        .accountStatus(savedVirtualAccount.getAccountStatus())
                        .balance(savedVirtualAccount.getBalance())
                        .currency(savedVirtualAccount.getCurrency())
                        .employerId(String.valueOf(EMPLOYER().getEmployerId()))
                        .build();

                response.setStatusCode(REQUEST_SUCCESS);
                response.setStatusMessage("Virtual bank account created successfully");
                response.setData(virtualAccountDTO);
            } else {
                log.warn("Failed to create virtual account for employer: {}", EMPLOYER().getEmployerId());
                response.setStatusCode(STATUS_400);
                response.setStatusMessage("Unable to create virtual account");
            }
        } catch (Exception e) {
            log.error("Error creating virtual account: {}", e.getMessage());
            throw new RuntimeException(e);
        }

        return response;
    }

    @Override
    public DefaultApiResponse<VirtualAccountDTO> getVirtualAccount() {
        DefaultApiResponse<VirtualAccountDTO> response = new DefaultApiResponse<>();
        VirtualAccountDTO virtualAccountDTO;

        try {
            // Retrieve virtual account associated with employer
            Optional<VirtualAccount> optionalVirtualAccount =
                    virtualAccountRepository.findByEmployer_EmployerId(EMPLOYER().getEmployerId());

            if (optionalVirtualAccount.isPresent()) {
                VirtualAccount virtualAccount = optionalVirtualAccount.get();

                // Build response DTO with retrieved account details
                virtualAccountDTO = VirtualAccountDTO.builder()
                        .virtualAccountId(String.valueOf(virtualAccount.getAccountId()))
                        .accountNumber(virtualAccount.getAccountNumber())
                        .bankCode(virtualAccount.getBankCode())
                        .bankName(virtualAccount.getBankName())
                        .accountStatus(virtualAccount.getAccountStatus())
                        .balance(virtualAccount.getBalance())
                        .currency(virtualAccount.getCurrency())
                        .employerId(String.valueOf(EMPLOYER().getEmployerId()))
                        .build();

                response.setStatusCode(REQUEST_SUCCESS);
                response.setStatusMessage("Virtual bank account retrieved successfully");
                response.setData(virtualAccountDTO);
            } else {
                log.warn("No virtual account found for employer: {}", EMPLOYER().getEmployerId());
                response.setStatusCode(STATUS_400);
                response.setStatusMessage("No virtual account found");
            }
        } catch (Exception e) {
            log.error("Error retrieving virtual account: {}", e.getMessage());
            throw new RuntimeException(e);
        }

        return response;
    }

    @Override
    public DefaultApiResponse<VirtualAccountTransactionDTO> getTransactionsOfVba(
            String startDate, String endDate, Integer page, Integer limit) {
        DefaultApiResponse<VirtualAccountTransactionDTO> response = new DefaultApiResponse<>();

        try {
            // Retrieve virtual account linked to employer
            Optional<VirtualAccount> optionalVirtualAccount =
                    virtualAccountRepository.findByEmployer_EmployerId(EMPLOYER().getEmployerId());

            if (optionalVirtualAccount.isPresent()) {
                VirtualAccount virtualAccount = optionalVirtualAccount.get();

                // Fetch transactions from external KoraPay service
                DefaultKoraResponse<VBATransactionDTO> responseBody = koraPayService.getTransactionOfVBA(
                        virtualAccount.getAccountNumber(), EMPLOYER(), startDate, endDate, page, limit);

                if (responseBody.getMessage().equals("Virtual bank account transactions retrieved successfully")) {
                    VBATransactionDTO data = responseBody.getData();
                    List<VirtualAccountTransactionDTO.TransactionDTO> transactions = new ArrayList<>();

                    // Loop through and map the transaction details
                    for (VBATransactionDTO.TransactionDTO transaction : data.getTransactions()) {
                        transactions.add(VirtualAccountTransactionDTO.TransactionDTO.builder()
                                .payerAccountNumber(transaction.getPayer_bank_account().getAccount_number())
                                .payerAccountName(transaction.getPayer_bank_account().getAccount_name())
                                .payerBankName(transaction.getPayer_bank_account().getBankName())
                                .reference(transaction.getReference())
                                .description(transaction.getDescription())
                                .status(transaction.getStatus())
                                .amount(transaction.getAmount())
                                .fee(transaction.getFee())
                                .currency(transaction.getCurrency())
                                .build());
                    }

                    // Build response DTO with transactions
                    VirtualAccountTransactionDTO transactionDTO = VirtualAccountTransactionDTO.builder()
                            .totalPages(data.getPagination().getTotal_pages())
                            .transactions(transactions)
                            .build();

                    response.setStatusCode(REQUEST_SUCCESS);
                    response.setStatusMessage("Transactions for Bank Account Retrieved Successfully");
                    response.setData(transactionDTO);
                } else {
                    log.warn("Failed to retrieve transactions for account: {}", virtualAccount.getAccountNumber());
                    response.setStatusCode("49");
                    response.setStatusMessage("Unable to get transactions for virtual account");
                }
            }
        } catch (Exception e) {
            log.error("Error retrieving transactions: {}", e.getMessage());
            throw new RuntimeException(e);
        }

        return response;
    }

    @Override
    public DefaultApiResponse<BankTransferDetailsDTO> processBankTransfer(BigDecimal amount) {
        DefaultApiResponse<BankTransferDetailsDTO> response = new DefaultApiResponse<>();

        try {
            // Call external KoraPay service to initiate bank transfer
            DefaultKoraResponse<BankTransferResponseDTO> responseBody = koraPayService.initiateBankTransfer(amount, EMPLOYER());

            if (responseBody.getMessage().equals("Bank transfer initiated successfully")) {
                BankTransferResponseDTO data = responseBody.getData();

                // Build response DTO with transfer details
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

                response.setStatusCode(REQUEST_SUCCESS);
                response.setStatusMessage("Bank transfer created successfully");
                response.setData(detailsForTransfer);
            } else {
                log.warn("Failed to initiate bank transfer");
                response.setStatusCode(STATUS_400);
                response.setStatusMessage("Bank transfer initialization failed");
            }
        } catch (Exception e) {
            log.error("Error initiating bank transfer: {}", e.getMessage());
            throw new RuntimeException(e);
        }

        return response;
    }

    @Override
    public DefaultApiResponse<?> verifyBankTransfer(String referenceNumber) {
        DefaultApiResponse<?> response = new DefaultApiResponse<>();

        try {
            // Retrieve virtual account linked to employer
            Optional<VirtualAccount> optionalVirtualAccount =
                    virtualAccountRepository.findByEmployer_EmployerId(EMPLOYER().getEmployerId());

            if (optionalVirtualAccount.isPresent()) {
                VirtualAccount virtualAccount = optionalVirtualAccount.get();

                // Fetch webhook data to verify transfer status
                Optional<WebhookData> webhookDataOptional = webhookDataRepository.findByReference(referenceNumber);
                if (webhookDataOptional.isPresent()) {
                    WebhookData webhookData = webhookDataOptional.get();

                    // Check if the transfer event was successful
                    if (webhookData.getEvent().equals("charge.success")) {
                        virtualAccount.setBalance(webhookData.getAmount());
                        virtualAccountRepository.save(virtualAccount);
                        response.setStatusCode("00");
                        response.setStatusMessage("Bank transfer successful");
                    } else {
                        log.warn("Bank transfer failed for reference: {}", referenceNumber);
                        response.setStatusCode("49");
                        response.setStatusMessage("Bank transfer failed");
                    }
                }
            }
        } catch (Exception e) {
            log.error("Error verifying bank transfer: {}", e.getMessage());
            throw new RuntimeException(e);
        }

        return response;
    }
}
