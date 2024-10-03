package com.aalto.paycraft.service.impl;

import com.aalto.paycraft.dto.*;
import com.aalto.paycraft.entity.*;
import com.aalto.paycraft.mapper.PaymentMapper;
import com.aalto.paycraft.repository.*;
import com.aalto.paycraft.service.IKoraPayService;
import com.aalto.paycraft.service.IPaymentService;
import com.aalto.paycraft.service.JWTService;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import jakarta.servlet.http.HttpServletRequest;
import lombok.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;
import java.util.stream.Collectors;

import static com.aalto.paycraft.constants.PayCraftConstant.REQUEST_SUCCESS;
import static com.aalto.paycraft.constants.PayCraftConstant.STATUS_400;

@Slf4j
@Service
@RequiredArgsConstructor
public class PaymentServiceImpl implements IPaymentService {
    private final PayrollRepository payrollRepository;
    private final PaymentRepository paymentRepository;
    private final WebhookDataRepository webhookDataRepository;
    private final VirtualAccountRepository virtualAccountRepository;
    private final EmployeeRepository employeeRepository;
    private final EmployerRepository employerRepository;
    private final HttpServletRequest request;
    private final JWTService jwtService;
    private final IKoraPayService koraPayService; // Service for handling payouts

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

    @Override
    public DefaultApiResponse<List<String>> getBankNames() throws Exception {
        DefaultKoraResponse<List<BankTypeDTO>> bankListResponse = koraPayService.listBanks();

        DefaultApiResponse<List<String>> response = new DefaultApiResponse<>();

        if (bankListResponse.getMessage().equals("Successful")) {
            // Use Java Streams to extract the bank names from the response
            response.setStatusCode(REQUEST_SUCCESS);
            response.setStatusMessage("Bank List Retrieved");
            response.setData(bankListResponse.getData().stream()
                    .map(BankTypeDTO::getName)  // Map to bank name
                    .collect(Collectors.toList()));
            return response;  // Collect as a List
        } else {
            throw new RuntimeException("Failed to fetch banks: " + bankListResponse.getMessage());
        }
    }

    private String getBankCodeByName(String bankName) throws Exception {
        DefaultKoraResponse<List<BankTypeDTO>> bankListResponse = koraPayService.listBanks();

        if (bankListResponse.getMessage().equals("Successful")) {
            // Use Java Streams to filter the bank by name and get the bank code
            return bankListResponse.getData().stream()
                    .filter(bank -> bank.getName().equals(bankName))  // Filter by bank name
                    .findFirst()  // Get the first matching bank
                    .map(BankTypeDTO::getCode)  // Map to bank code
                    .orElseThrow(() -> new RuntimeException("Bank not found: " + bankName));  // Throw exception if not found
        } else {
            throw new RuntimeException("Failed to fetch banks: " + bankListResponse.getMessage());
        }
    }


    @Override
    public DefaultApiResponse<PaymentDTO> payEmployee(UUID employeeId) {
        DefaultApiResponse<PaymentDTO> apiResponse = new DefaultApiResponse<>();
        Payment payment;

        VirtualAccount virtualAccount = new VirtualAccount();
        try {
            // Fetch employee by ID
            Employee employee = employeeRepository.findByEmployeeId(employeeId)
                    .orElseThrow(() -> new RuntimeException("Employee not found"));

            // Retrieve virtual account linked to employer
            Optional<VirtualAccount> optionalVirtualAccount =
                    virtualAccountRepository.findByEmployer_EmployerId(EMPLOYER().getEmployerId());

            if (optionalVirtualAccount.isPresent()) {
                virtualAccount = optionalVirtualAccount.get();
            }

            if(virtualAccount.getBalance().compareTo(employee.getSalaryAmount()) < 0){
                apiResponse.setStatusCode(STATUS_400);
                apiResponse.setStatusMessage("Insufficient funds to make this payment: Balance is " +  virtualAccount.getBalance());
                log.info("Insufficient funds to make this payments: {}", virtualAccount.getBalance());
                return apiResponse;
            }

            // Get bank code based on employee's bank name (e.g., "United Bank of Africa")
            String bankCode = getBankCodeByName(employee.getBankName());

            // Resolve bank account using bank code and account number
            DefaultKoraResponse<BankAccountDTO> resolveResponse = null;
            try {
                resolveResponse = koraPayService.resolveBankAccount(bankCode, employee.getAccountNumber());
            } catch (Exception e) {
                throw new RuntimeException(e.getMessage());
            }

            if (resolveResponse.getMessage().equals("Request completed")) {
                BankAccountDTO resolvedAccount = resolveResponse.getData();


                    // Ensure the resolved account matches the employee's account
                log.info("Resolved: {}",resolvedAccount.getAccount_number());
                log.info("Employee: {}", employee.getAccountNumber());
                if (resolvedAccount.getAccount_number().equalsIgnoreCase(employee.getAccountNumber())) {
                    // Request payout to the resolved employee account
                    DefaultKoraResponse<PayoutResponseDTO> payoutResponse = koraPayService.requestPayout(
                            bankCode,
                            employee.getAccountNumber(),
                            employee.getSalaryAmount(),
                            EMPLOYER()
                    );

                    if ("Transfer initiated successfully.".equals(payoutResponse.getMessage())) {

                        payment = Payment.builder()
                                .referenceNumber(payoutResponse.getData().getReference())
                                .amount(payoutResponse.getData().getAmount())
                                .transactionType("DEBIT")
                                .transactionDateTime(LocalDateTime.now())
                                .description(payoutResponse.getData().getNarration())
                                .currency(payoutResponse.getData().getCurrency())
                                .payrollName(null)
                                .employeeName(String.format("%s %s", employee.getFirstName(), employee.getLastName()))
                                .account(virtualAccount)
                                .build();

                        apiResponse.setStatusCode(REQUEST_SUCCESS);
                        apiResponse.setStatusMessage("Payout request completed");
                        apiResponse.setData(PaymentMapper.toDTO(payment));

                        try {
                            // Wait for 5 seconds (5000 milliseconds)
                            Thread.sleep(6000);
                            // Call the verifyPayment method after the delay
                            apiResponse = verifyPayment(payment.getReferenceNumber());
                        } catch (InterruptedException e) {
                            throw new RuntimeException(e.getMessage());
                        }

                        log.info("Payout successful for employee: {}", employee.getFirstName());
                    } else {
                        apiResponse.setStatusCode(STATUS_400);
                        apiResponse.setStatusMessage("Payout request failed");
                        log.error("Payout failed for employee {}: {}", employee.getFirstName(), payoutResponse.getMessage());
                    }
                } else {
                    apiResponse.setStatusCode(STATUS_400);
                    apiResponse.setStatusMessage("Payout request failed: Account Number resolved does not match");
                    log.error("Account Number mismatch for employee: expected {}, got {}",
                            employee.getAccountNumber(),
                            resolvedAccount.getAccount_number());
                }
            } else {
                apiResponse.setStatusCode(STATUS_400);
                apiResponse.setStatusMessage("Payout request failed: Account Number could not be resolved");
                log.error("Failed to resolve bank account for employee {}: {}", employee.getFirstName(), resolveResponse.getMessage());
            }
        } catch (Exception e) {
            apiResponse.setStatusCode(STATUS_400);
            apiResponse.setStatusMessage("Payout request failed: " + e.getMessage());
            log.error("Error while processing payout for employee {}: {}", employeeId, e.getMessage());
        }

        return apiResponse;
    }

    @Override
    public DefaultApiResponse<BulkPayoutResponseDTO> payEmployeesBulk(UUID payrollId) {
        DefaultApiResponse<BulkPayoutResponseDTO> apiResponse = new DefaultApiResponse<>();
        String reference;

        Payroll payroll = new Payroll();
        VirtualAccount virtualAccount = new VirtualAccount();
        List<PayoutData> payoutDataList = new ArrayList<>();

        try {
            Optional<Payroll> payrollOptional = payrollRepository.findByPayrollId(payrollId);
            if (payrollOptional.isPresent()) {
                payroll = payrollOptional.get();
            }

            List<Employee> employees = payroll.getEmployees();

            // Retrieve virtual account linked to employer
            Optional<VirtualAccount> optionalVirtualAccount =
                    virtualAccountRepository.findByEmployer_EmployerId(EMPLOYER().getEmployerId());

            if (optionalVirtualAccount.isPresent()) {
                virtualAccount = optionalVirtualAccount.get();
            }

            BigDecimal total = BigDecimal.ZERO;
            BigDecimal totalPayrollSalary = BigDecimal.ZERO;
            for(Employee employee : employees) {
                total = totalPayrollSalary.add(employee.getSalaryAmount());
            }

            if (virtualAccount.getBalance().compareTo(total) < 0) {
                apiResponse.setStatusCode(STATUS_400);
                apiResponse.setStatusMessage("Insufficient funds to run this payroll: Balance is " + virtualAccount.getBalance());
                log.info("Insufficient Funds to run this Payroll: {}", virtualAccount.getBalance());
                return apiResponse;
            }

            try{
                for(Employee employee : employees) {
                    PayoutData payoutData = PayoutData.builder()
                            .fullName(String.format("%s %s", employee.getFirstName(), employee.getLastName()))
                            .email(employee.getEmailAddress())
                            .accountNumber(employee.getAccountNumber())
                            .amount(employee.getSalaryAmount())
                            .bankCode(getBankCodeByName(employee.getBankName()))
                            .currency("NGN")
                            .build();

                    payoutDataList.add(payoutData);
                }

                DefaultKoraResponse<BulkPayoutResponseDTO> responseBody = koraPayService.requestBulkPayout(payoutDataList, EMPLOYER());
                log.info(String.valueOf(responseBody.getData()));

                if(responseBody.getMessage().equals("Bulk payout initiated successfully")){
                    reference = responseBody.getData().getReference();

                    Payment payment = Payment.builder()
                            .referenceNumber(responseBody.getData().getReference())
                            .amount(responseBody.getData().getTotal_chargeable_amount())
                            .transactionType("DEBIT")
                            .transactionDateTime(LocalDateTime.now())
                            .description(responseBody.getData().getDescription())
                            .currency(responseBody.getData().getCurrency())
                            .payrollName(payroll.getPayrollName())
                            .employeeName(null)
                            .account(virtualAccount)
                            .build();

                    paymentRepository.save(payment);

                    try {
                        // Wait for 5 seconds (5000 milliseconds)
                        Thread.sleep(6000);
                        // Call the verifyPayment method after the delay
//                        String message = verifyPayment(payment.getReferenceNumber()).getStatusMessage();
//
//                        if(message.equals("transfer.success")){
//                            apiResponse.setStatusCode(REQUEST_SUCCESS);
//                            apiResponse.setStatusMessage("Bulk Payout Completed: " + reference);
//                            apiResponse.setData(responseBody.getData());
//
//                            return apiResponse;
//                        } else {
//                            apiResponse.setStatusCode(STATUS_400);
//                            apiResponse.setStatusMessage("Payout request failed");
//                            log.error("Payout failed for payoll with ID: {}", payroll.getPayrollId());
//                        }
                        virtualAccount.setBalance(virtualAccount.getBalance().subtract(payment.getAmount()));
                        virtualAccountRepository.save(virtualAccount);

                        apiResponse.setStatusCode(REQUEST_SUCCESS);
                        apiResponse.setStatusMessage("Bulk Payout Completed: " + reference);
                        apiResponse.setData(responseBody.getData());

                    } catch (InterruptedException e) {
                        throw new RuntimeException(e.getMessage());
                    }

                }else {
                    apiResponse.setStatusCode(STATUS_400);
                    apiResponse.setStatusMessage("Payout request failed : " + responseBody.getMessage());
                }
            } catch (Exception e) {
                apiResponse.setStatusCode(STATUS_400);
                apiResponse.setStatusMessage("Payout request failed: " + e.getMessage());
            }

        } catch (RuntimeException ex){
            apiResponse.setStatusCode(STATUS_400);
            apiResponse.setStatusMessage("Payout request failed: " + ex.getMessage());
            log.error("Payout request failed: {}", ex.getMessage());
        }

        return apiResponse;
    }


    // Verify token expiration
    private void verifyTokenExpiration(String token) {
        if (jwtService.isTokenExpired(token)) {
            log.warn("Token has expired for employer {}", EMPLOYER());
            throw new ExpiredJwtException(null, null, "Access Token has expired");
        }
    }

    @Override
    public DefaultApiResponse<PaymentDTO> verifyPayment(String referenceNumber) {
        int retryCount = 0;
        int maxRetries = 10;
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
                        System.out.println("Waiting for webhook data...");
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
                    if (webhookData.getEvent().equals("transfer.success")) {
                        virtualAccount.setBalance(virtualAccount.getBalance().subtract(webhookData.getAmount()));
                        virtualAccountRepository.save(virtualAccount);

                        Payment payment = Payment.builder()
                                .referenceNumber(webhookData.getReference())
                                .amount(webhookData.getAmount())
                                .transactionType("DEBIT")
                                .transactionDateTime(LocalDateTime.now())
                                .description("Employee Payment")
                                .currency(webhookData.getCurrency())
                                .payrollName(null)
                                .employeeName(String.format("%s %s", EMPLOYER().getFirstName(), EMPLOYER().getLastName()))
                                .account(virtualAccount)
                                .build();

                        paymentRepository.save(payment);

                        response.setStatusCode("00");
                        response.setStatusMessage("Bank Payout successful");
                        response.setData(PaymentMapper.toDTO(payment));

                    } else {
                        log.warn("Bank transfer failed for reference: {}", referenceNumber);
                        response.setStatusCode("49");
                        response.setStatusMessage("Bank Payout failed");
                    }
                } else {
                    response.setStatusCode("49");
                    response.setStatusMessage("Bank Payout failed");
                }
            }
        } catch (Exception e) {
            log.error("Error verifying payout: {}", e.getMessage());
            throw new RuntimeException(e);
        }

        return response;
    }

}
