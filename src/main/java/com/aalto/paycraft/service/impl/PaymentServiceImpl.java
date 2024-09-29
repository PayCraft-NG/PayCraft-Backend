package com.aalto.paycraft.service.impl;

import com.aalto.paycraft.dto.*;
import com.aalto.paycraft.entity.*;
import com.aalto.paycraft.repository.*;
import com.aalto.paycraft.service.IKoraPayService;
import com.aalto.paycraft.service.IPaymentService;
import com.aalto.paycraft.service.JWTService;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import jakarta.persistence.*;
import jakarta.servlet.http.HttpServletRequest;
import lombok.*;
import lombok.extern.slf4j.Slf4j;
import org.hibernate.annotations.JdbcTypeCode;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.sql.Types;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;

import static com.aalto.paycraft.constants.PayCraftConstant.REQUEST_SUCCESS;
import static com.aalto.paycraft.constants.PayCraftConstant.STATUS_400;

@Slf4j
@Service
@RequiredArgsConstructor
public class PaymentServiceImpl implements IPaymentService {
    private final PayrollRepository payrollRepository;
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

    public String getBankCodeByName(String bankName) throws Exception {
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
        Payment payment = new Payment();

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

            if (resolveResponse.getMessage().equals("Request Completed")) {
                BankAccountDTO resolvedAccount = resolveResponse.getData();

                    // Ensure the resolved account matches the employee's account
                if (resolvedAccount.getAccount_name().equalsIgnoreCase(employee.getAccountNumber())) {
                    // Request payout to the resolved employee account
                    DefaultKoraResponse<PayoutResponseDTO> payoutResponse = koraPayService.requestPayout(
                            bankCode,
                            employee.getAccountNumber(),
                            employee.getSalaryAmount(),
                            EMPLOYER()
                    );

                    if ("Request Completed".equals(payoutResponse.getMessage())) {

                        payment = Payment.builder()
                                .referenceNumber(payoutResponse.getData().getReference())
                                .amount(payoutResponse.getData().getAmount())
                                .transactionType("DEBIT")
                                .transactionDateTime(payoutResponse.getData().)
                                .description()
                                .currency()
                                .payrollName()
                                .employeeName()
                                .account()
                                .build();

                        apiResponse.setStatusCode(REQUEST_SUCCESS);
                        apiResponse.setStatusMessage("Payout request completed");
                        apiResponse.setData();

                        log.info("Payout successful for employee: {}", employee.getFirstName());
                    } else {
                        log.error("Payout failed for employee {}: {}", employee.getFirstName(), payoutResponse.getMessage());
                    }
                } else {
                    log.error("Account Number mismatch for employee: expected {}, got {}",
                            employee.getAccountNumber(),
                            employee.getAccountNumber());
                }
            } else {
                log.error("Failed to resolve bank account for employee {}: {}", employee.getFirstName(), resolveResponse.getMessage());
            }
        } catch (Exception e) {
            log.error("Error while processing payout for employee {}: {}", employeeId, e.getMessage());
        }

        return apiResponse;
    }




//    @Override
//    public void payEmployeeBulk(UUID payrollId) {
//        try {
//            // Fetch payroll by ID
//            Payroll payroll = payrollRepository.findById(payrollId)
//                    .orElseThrow(() -> new RuntimeException("Payroll not found"));
//
//            List<Employee> employees = payroll.getEmployees();
//
//            // Collect payout data for all employees in the payroll
//            List<PayoutData> payoutDataList = payroll.getEmployees().stream()
//                    .map(employee -> new PayoutData(
//                            employee.getSalaryAmount(),
//                            employee.getBankCode(),
//                            employee.getAccountNumber()
//                    ))
//                    .toList();
//
//            // Request bulk payout for all employees
//            DefaultKoraResponse<BulkPayoutResponseDTO> bulkPayoutResponse = paymentGatewayService.requestBulkPayout(payoutDataList, payroll.getCompany());
//
//            if (bulkPayoutResponse.getStatus()) {
//                log.info("Bulk payout successful for payroll ID: {}", payrollId);
//            } else {
//                log.error("Bulk payout failed for payroll ID {}: {}", payrollId, bulkPayoutResponse.getMessage());
//            }
//
//        } catch (Exception e) {
//            log.error("Error while processing bulk payout for payroll ID {}: {}", payrollId, e.getMessage());
//            // Handle exceptions (e.g., log them, notify someone, etc.)
//        }
//    }

    // Verify token expiration
    private void verifyTokenExpiration(String token) {
        if (jwtService.isTokenExpired(token)) {
            log.warn("Token has expired for employer {}", EMPLOYER());
            throw new ExpiredJwtException(null, null, "Access Token has expired");
        }
    }

    public DefaultApiResponse<?> verifyPayment(String referenceNumber) {
        int retryCount = 0;
        int maxRetries = 5;
        long retryInterval = 5000; // 5 seconds

        DefaultApiResponse<?> response = new DefaultApiResponse<>();

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
