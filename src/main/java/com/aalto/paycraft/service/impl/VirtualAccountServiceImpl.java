package com.aalto.paycraft.service.impl;

import com.aalto.paycraft.dto.*;
import com.aalto.paycraft.dto.enums.Currency;
import com.aalto.paycraft.entity.*;
import com.aalto.paycraft.repository.*;
import com.aalto.paycraft.service.IKoraPayService;
import com.aalto.paycraft.service.IVirtualAccountService;
import com.aalto.paycraft.service.JWTService;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;

import static com.aalto.paycraft.constants.PayCraftConstant.REQUEST_SUCCESS;
import static com.aalto.paycraft.constants.PayCraftConstant.STATUS_400;

@Service
@RequiredArgsConstructor
@Slf4j
public class VirtualAccountServiceImpl implements IVirtualAccountService {
    private final VirtualAccountRepository virtualAccountRepository;
    private final CardRepository cardRepository;
    private final WebhookDataRepository webhookDataRepository;
    private final PaymentRepository paymentRepository;
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
    public UUID createVirtualAccount(Employer employer) {
        DefaultApiResponse<VirtualAccountDTO> response = new DefaultApiResponse<>();
        VirtualAccount virtualAccount = new VirtualAccount();
        VirtualAccountDTO virtualAccountDTO;

        try {
            // Call external KoraPay service to create a virtual account
            DefaultKoraResponse<VirtualAccountResponseDTO> responseBody = koraPayService.createVirtualAccount(employer);

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
                        .employer(employer)  // Link to employer
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
                        .employerId(String.valueOf(employer.getEmployerId()))
                        .build();

                response.setStatusCode(REQUEST_SUCCESS);
                response.setStatusMessage("Virtual bank account created successfully");
                response.setData(virtualAccountDTO);
            } else {
                log.warn("Failed to create virtual account for employer: {}", employer);
                response.setStatusCode(STATUS_400);
                response.setStatusMessage("Unable to create virtual account");
            }
        } catch (Exception e) {
            log.error("Error creating virtual account: {}", e.getMessage());
            throw new RuntimeException(e);
        }

        return virtualAccount.getAccountId();
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

                // Fetch payments from external KoraPay service
                DefaultKoraResponse<VBATransactionDTO> responseBody = koraPayService.getTransactionOfVBA(
                        virtualAccount.getAccountNumber(), EMPLOYER(), startDate, endDate, page, limit);

                if (responseBody.getMessage().equals("Virtual bank account transactions retrieved successfully")) {
                    VBATransactionDTO data = responseBody.getData();
                    List<VirtualAccountTransactionDTO.TransactionDTO> transactions = new ArrayList<>();

                    // Loop through and map the transaction details
                    // Only the ones made directly to the Virtual Account Number
                    for (VBATransactionDTO.TransactionDTO transaction : data.getTransactions()) {
                        transactions.add(VirtualAccountTransactionDTO.TransactionDTO.builder()
                                .payerAccountNumber(transaction.getPayer_bank_account().getAccount_number())
                                .payerAccountName(transaction.getPayer_bank_account().getAccount_name())
                                .payerBankName(transaction.getPayer_bank_account().getBankName())
                                .reference(transaction.getReference())
                                .description(transaction.getDescription())
                                .status(transaction.getStatus())
                                .amount(String.valueOf(transaction.getAmount()))
                                .fee(String.valueOf(transaction.getFee()))
                                .currency(transaction.getCurrency())
                                .build());
                    }

                    // Build response DTO with payments
                    VirtualAccountTransactionDTO transactionDTO = VirtualAccountTransactionDTO.builder()
                            .totalPages(data.getPagination().getTotal_pages())
                            .transactions(transactions)
                            .build();

                    response.setStatusCode(REQUEST_SUCCESS);
                    response.setStatusMessage("Payment for Bank Account Retrieved Successfully");
                    response.setData(transactionDTO);
                } else {
                    log.warn("Failed to retrieve payments for account: {}", virtualAccount.getAccountNumber());
                    response.setStatusCode("49");
                    response.setStatusMessage("Unable to get payments for virtual account");
                }
            }
        } catch (Exception e) {
            log.error("Error retrieving payments: {}", e.getMessage());
            throw new RuntimeException(e);
        }

        return response;
    }

    @Override
    public DefaultApiResponse<PaymentDataResponseDTO> getAllPayments(int pageSize, int pageNumber) {
        Pageable pageable = PageRequest.of(pageNumber, pageSize);

        DefaultApiResponse<PaymentDataResponseDTO> response = new DefaultApiResponse<>();
        PaymentDataResponseDTO data = new PaymentDataResponseDTO();
        VirtualAccount virtualAccount = new VirtualAccount();

        try {
            // Retrieve virtual account linked to employer
            Optional<VirtualAccount> optionalVirtualAccount =
                    virtualAccountRepository.findByEmployer_EmployerId(EMPLOYER().getEmployerId());

            if (optionalVirtualAccount.isPresent()) {
                virtualAccount = optionalVirtualAccount.get();

            }

            // Fetch payments from repository with pagination
            int totalPageSize = paymentRepository.findAllByAccount_AccountId(virtualAccount.getAccountId()).size();

            Page<Payment> payments = paymentRepository.findAllByAccount_AccountIdOrderByTransactionDateTimeDesc(
                    virtualAccount.getAccountId(), pageable);

            // Convert Payment entities to PaymentDTO
            List<PaymentDTO> paymentDTOList = payments.stream()
                    .map(this::convertToPaymentDTO)
                    .toList();

            data.setTotalPages(totalPageSize / pageSize);
            data.setPageSize(totalPageSize);
            data.setPayments(paymentDTOList);

            // Set the response with payment DTOs and pagination information
            response.setStatusCode(REQUEST_SUCCESS);
            response.setStatusMessage("Payments fetched successfully");
            response.setData(data);
        } catch (Exception e) {
            response.setStatusCode(STATUS_400);
            response.setStatusMessage("Error retrieving payments: " + e.getMessage());

            return response;
        }

        return response;
    }

    private PaymentDTO convertToPaymentDTO(Payment payment) {
        return PaymentDTO.builder()
                .referenceNumber(payment.getReferenceNumber())
                .amount(payment.getAmount())
                .transactionType(payment.getTransactionType())
                .transactionDateTime(payment.getTransactionDateTime())
                .currency(payment.getCurrency())
                .description(payment.getDescription())
                .payrollName(payment.getPayrollName())
                .employeeName(payment.getEmployeeName())
                .build();
    }

    @Override
    public DefaultApiResponse<BankTransferDetailsDTO> processBankTransfer(BigDecimal amount) {
        DefaultApiResponse<BankTransferDetailsDTO> response = new DefaultApiResponse<>();

        try {
            // Call external KoraPay service to initiate bank transfer
            DefaultKoraResponse<BankTransferResponseDTO> responseBody = koraPayService.initiateBankTransfer(amount, EMPLOYER());

            log.info(responseBody.getMessage());
            log.info(responseBody.toString());

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
    public DefaultApiResponse<?> processCardFunding(CardFundingRequestDTO requestBody){
        DefaultApiResponse<?> response = new DefaultApiResponse<>();
        VirtualAccount virtualAccount = new VirtualAccount();
        try {
            // Retrieve virtual account linked to employer
            Optional<VirtualAccount> optionalVirtualAccount =
                    virtualAccountRepository.findByEmployer_EmployerId(EMPLOYER().getEmployerId());

            if (optionalVirtualAccount.isPresent()) {
                virtualAccount = optionalVirtualAccount.get();
            }

            // Call external KoraPay service to initiate bank transfer
            DefaultKoraResponse<PaymentDataDTO> responseBody = koraPayService.chargeCard(requestBody, EMPLOYER());

            if (responseBody.getMessage().equals("Card charged successfully")) {

                virtualAccount.setBalance(responseBody
                        .getData().getAmount().add(virtualAccount.getBalance()));
                virtualAccountRepository.save(virtualAccount);

                Payment payment = Payment.builder()
                        .account(virtualAccount)
                        .amount(responseBody.getData().getAmount())
                        .currency(responseBody.getData().getCurrency())
                        .employeeName(null)
                        .payrollName(null)
                        .referenceNumber(responseBody.getData().getPayment_reference())
                        .description("Fund Account via Card")
                        .transactionType("CREDIT")
                        .transactionDateTime(LocalDateTime.now())
                        .build();
                paymentRepository.save(payment);

                response.setStatusCode(REQUEST_SUCCESS);
                response.setStatusMessage("Account CREDITED successfully");
            } else {
                log.warn("Card Funding failed {}", responseBody.getMessage());
                response.setStatusCode(STATUS_400);
                response.setStatusMessage("Failed to fund account via Card: " + responseBody.getMessage());
            }
        } catch (Exception e) {
            log.error("Card Funding failed");
            throw new RuntimeException(e);
        }
        return response;
    }



    @Override // This would work for both fixedVirtualAccount or BankTransfer
    public DefaultApiResponse<PaymentDTO> verifyPayment(String referenceNumber) {
        int retryCount = 0;
        int maxRetries = 5;
        long retryInterval = 5000; // 5 seconds

        DefaultApiResponse<PaymentDTO> response = new DefaultApiResponse<>();

        try {
            // Retrieve virtual account linked to employer
            Optional<VirtualAccount> optionalVirtualAccount =
                    virtualAccountRepository.findByEmployer_EmployerId(EMPLOYER().getEmployerId());

            if (optionalVirtualAccount.isPresent()) {
                VirtualAccount virtualAccount = optionalVirtualAccount.get();

                // Fetch webhook data to verify transfer status
                Optional<WebhookData> webhookDataOptional = webhookDataRepository.findByReference(referenceNumber);

                // Retry if the webhook data is not present
                while (webhookDataOptional.isEmpty() && retryCount < maxRetries) {
                    try {
                        log.info("Waiting for webhook data...");
                        TimeUnit.MILLISECONDS.sleep(retryInterval);  // Wait for 2 seconds

                        // Check for the webhook data again
                        webhookDataOptional = webhookDataRepository.findByReference(referenceNumber);

                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();  // Restore interrupt status
                        throw new RuntimeException(e.getMessage());
                    }

                    retryCount++;
                }

                if (webhookDataOptional.isPresent()) {
                    WebhookData webhookData = webhookDataOptional.get();

                    // Check if the transfer event was successful
                    if (webhookData.getEvent().equals("charge.success")) {
                        virtualAccount.setBalance(webhookData.getAmount().add(virtualAccount.getBalance()));
                        virtualAccountRepository.save(virtualAccount);

                        Payment payment = Payment.builder()
                                .account(virtualAccount)
                                .amount(webhookData.getAmount())
                                .currency(webhookData.getCurrency())
                                .employeeName(null)
                                .payrollName(null)
                                .referenceNumber(webhookData.getReference())
                                .description("Fund Account")
                                .transactionType("CREDIT")
                                .transactionDateTime(LocalDateTime.now())
                                .build();

                        paymentRepository.save(payment);

                        response.setStatusCode(REQUEST_SUCCESS);
                        response.setStatusMessage("Bank transfer successful");
                        response.setData(convertToPaymentDTO(payment));

                    } else {
                        log.warn("Bank transfer failed for reference: {}", referenceNumber);
                        response.setStatusCode(STATUS_400);
                        response.setStatusMessage("Bank transfer failed");
                    }
                }else{
                    response.setStatusCode(STATUS_400);
                    response.setStatusMessage("Bank transfer failed");
                    return response;
                }
            }
        } catch (Exception e) {
            log.error("Error verifying bank transfer: {}", e.getMessage());
            throw new RuntimeException(e);
        }

        return response;
    }

    private VirtualAccount getVirtualAccountOfEmployer() {
        VirtualAccount virtualAccount = new VirtualAccount();
        // Retrieve virtual account linked to employer
        Optional<VirtualAccount> optionalVirtualAccount =
                virtualAccountRepository.findByEmployer_EmployerId(EMPLOYER().getEmployerId());

        if (optionalVirtualAccount.isPresent()) {
            virtualAccount = optionalVirtualAccount.get();
        }
        return virtualAccount;
    }

    @Override
    public DefaultApiResponse<List<CardRequestDTO>> getCardsForEmployer(){
        DefaultApiResponse<List<CardRequestDTO>> response;
        try {
            response = new DefaultApiResponse<>();

            VirtualAccount account = getVirtualAccountOfEmployer();
            List<Card> cards = cardRepository.findAllByAccount_AccountId(account.getAccountId());
            List<CardRequestDTO> cardData = new ArrayList<>();

            for (Card card : cards) {
                CardRequestDTO cardRequestDTO = toDto(card);
                cardData.add(cardRequestDTO);
            }

            response.setStatusCode(REQUEST_SUCCESS);
            response.setStatusMessage("Cards Retrieved successfully");
            response.setData(cardData);

        } catch (Exception e) {
            throw new RuntimeException(e.getMessage());
        }

        return response;
    }

    private CardRequestDTO toDto(Card card){
        return  CardRequestDTO.builder()
                .cardId(card.getCardId())
                .cardNumber(card.getCardNumber())
                .expiryMonth(card.getExpiryMonth())
                .expiryYear(card.getExpiryYear())
                .build();
    }

    @Override
    public DefaultApiResponse<CardRequestDTO> saveCard(CardRequestDTO requestBody){
        DefaultApiResponse<CardRequestDTO> response;
        try {
            response = new DefaultApiResponse<>();

            VirtualAccount account = getVirtualAccountOfEmployer();
            Card card = Card.builder()
                    .cardNumber(requestBody.getCardNumber())
                    .expiryMonth(requestBody.getExpiryMonth())
                    .expiryYear(requestBody.getExpiryYear())
                    .cardPin(requestBody.getCardPin())
                    .cvv(requestBody.getCvv())
                    .account(account)
                    .build();

            cardRepository.save(card);

            response.setStatusCode(REQUEST_SUCCESS);
            response.setStatusMessage("Card SAVED successfully");
            response.setData(toDto(card));

        } catch (Exception e) {
            throw new RuntimeException(e.getMessage());
        }
        return response;
    }

    @Override
    public DefaultApiResponse<?> deleteCard(Long cardId){
        DefaultApiResponse<CardRequestDTO> response;
        Card card = new Card();
        try {
            response = new DefaultApiResponse<>();

            VirtualAccount account = getVirtualAccountOfEmployer();
            Optional<Card> cardOptional = cardRepository.findById(cardId);
            if (cardOptional.isPresent()) {
                card = cardOptional.get();
            }
            account.getCards().remove(card);
            cardRepository.delete(card);

            response.setStatusCode(REQUEST_SUCCESS);
            response.setStatusMessage("Card Deleted successfully");

        } catch (Exception e) {
            throw new RuntimeException(e.getMessage());
        }
        return response;
    }
}
