package com.aalto.paycraft.service.impl;

import com.aalto.paycraft.dto.*;
import com.aalto.paycraft.entity.Employer;
import com.aalto.paycraft.service.IKoraPayService;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.math.BigDecimal;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.*;

@Slf4j
@Service
@RequiredArgsConstructor
public class KoraPayServiceImpl implements IKoraPayService {

    @Value("${kora-secret}")
    private String SECRET_KEY;

    @Value("${webhook-url}")
    private String WEBHOOK_URL;

    @Value("${encryption-key}")
    private String ENCRYPTION_KEY;

    @Value("${kora-public}")
    private String PUBLIC_KEY;

    private final HttpClient httpClient = HttpClient.newHttpClient();
    private final ObjectMapper jacksonObjectMapper;

    private final String BASE_URL = "https://api.korapay.com/merchant/api/v1/";

    // ========== VIRTUAL BANK ACCOUNT RELATED =========
    @Override
    public DefaultKoraResponse<VirtualAccountResponseDTO> createVirtualAccount(Employer employer) {
        // Create request body for the API call
        Map<String, Object> requestBody = createRequestBody(employer);

        // Initialize the default response object
        DefaultKoraResponse<VirtualAccountResponseDTO> response = new DefaultKoraResponse<>();

        try {
            // Convert the request body map to a JSON string
            String requestBodyJson = jacksonObjectMapper.writeValueAsString(requestBody);
            log.debug("Request body JSON: {}", requestBodyJson); // Log the request body for debugging

            // Build the HTTP request to create a virtual bank account
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(BASE_URL + "/virtual-bank-account"))
                    .header("Content-Type", "application/json")
                    .header("Authorization", "Bearer " + SECRET_KEY)
                    .POST(HttpRequest.BodyPublishers.ofString(requestBodyJson))
                    .build();
            log.info("Sending request to create virtual bank account for employer: {}", employer.getEmailAddress());

            // Send the request and capture the response
            HttpResponse<String> httpResponse = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

            // Handle successful response
            if (httpResponse.statusCode() == 200) {
                response = jacksonObjectMapper.readValue(httpResponse.body(),
                        new TypeReference<DefaultKoraResponse<VirtualAccountResponseDTO>>() {
                        });
                log.info("Virtual bank account created successfully for employer: {}", employer.getEmailAddress());
            } else {
                // Handle non-200 response from the API
                response.setStatus(false);
                response.setMessage("Error Creating Virtual Account: " + httpResponse.body());
                log.error("Failed to create virtual bank account. Status Code: {}, Response: {}",
                        httpResponse.statusCode(), httpResponse.body());
            }
        } catch (Exception e) {
            // Handle any exceptions during the process
            log.error("Error while creating virtual bank account for employer {}: {}", employer.getEmailAddress(), e.getMessage());
            throw new RuntimeException(e.getMessage());
        }

        // Return the final response object
        return response;
    }

    @Override
    public DefaultKoraResponse<VBATransactionDTO> getTransactionOfVBA(String accountNumber, Employer employer,
                                                                      String startDate, String endDate, Integer page, Integer limit) {

        // Initializing the default response object
        DefaultKoraResponse<VBATransactionDTO> response = new DefaultKoraResponse<>();
        try {
            // Build the base URL with the mandatory account number
            log.info(accountNumber);
            StringBuilder urlBuilder = new StringBuilder(BASE_URL + "virtual-bank-account/transactions?account_number=" + accountNumber);
            log.info(urlBuilder.toString());

            // Add optional parameters if provided
            if (startDate != null && !startDate.isEmpty()) {
                urlBuilder.append("&start_date=").append(startDate);
            }
            if (endDate != null && !endDate.isEmpty()) {
                urlBuilder.append("&end_date=").append(endDate);
            }
            if (page != null) {
                urlBuilder.append("&page=").append(page);
            }
            if (limit != null) {
                urlBuilder.append("&limit=").append(limit);
            }

            // Build the HTTP request to create a virtual bank account
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(urlBuilder.toString()))
                    .header("Content-Type", "application/json")
                    .header("Authorization", "Bearer " + SECRET_KEY)
                    .GET().build();

            log.info("Sending request to fetch VBA payments for employer: {}", employer.getEmailAddress());

            // Send the request and capture the response
            HttpResponse<String> httpResponse = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

            // Handle successful response
            if (httpResponse.statusCode() == 200) {
                response = jacksonObjectMapper.readValue(httpResponse.body(),
                        new TypeReference<DefaultKoraResponse<VBATransactionDTO>>() {
                        });
                log.info("VBA Payment fetched successfully for employer: {}", employer.getEmailAddress());
                log.info(response.toString());
            } else {
                // Handle non-200 response from the API
                response.setStatus(false);
                response.setMessage("Error Fetching Payment: " + httpResponse.body());
                log.error("Error Fetching Payment: {}", httpResponse.body());
            }
        } catch (Exception e) {
            // Handle any exceptions during the process
            log.error("Error Fetching Payment for employer {}: {}", employer.getEmailAddress(), e.getMessage());
            throw new RuntimeException(e.getMessage());
        }
        // Return the final response object
        return response;
    }

    private Map<String, Object> createRequestBody(Employer employer) {
        Map<String, Object> requestBody = new HashMap<>();
        String fullName = String.format("%s %s", employer.getFirstName(), employer.getLastName());
        requestBody.put("account_name", fullName);
        requestBody.put("account_reference", generateRef().substring(0, 10));
        requestBody.put("permanent", true);
        requestBody.put("bank_code", "000");

        // Customer information
        Map<String, String> customer = new HashMap<>();
        customer.put("name", fullName);
        customer.put("email", employer.getEmailAddress());
        requestBody.put("customer", customer);

        // KYC information
        Map<String, String> kyc = new HashMap<>();
        kyc.put("bvn", employer.getBvn());
        requestBody.put("kyc", kyc);
        return requestBody;
    }


    // ==========  BANK TRANSFER RELATED OPERATION =========
    @Override
    public DefaultKoraResponse<BankTransferResponseDTO> initiateBankTransfer(BigDecimal amount, Employer employer) {
        HttpResponse<String> httpResponse;
        DefaultKoraResponse<BankTransferResponseDTO> response = new DefaultKoraResponse<>();
        try {
            // Prepare request body
            Map<String, Object> requestBody = createRequestBodyForTransfer(amount, "NGN", employer);

            // Convert request body to JSON
            String requestBodyJson = jacksonObjectMapper.writeValueAsString(requestBody);

            // Build the request
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(BASE_URL + "charges/bank-transfer"))
                    .header("Content-Type", "application/json")
                    .header("Authorization", "Bearer " + SECRET_KEY)
                    .POST(HttpRequest.BodyPublishers.ofString(requestBodyJson))
                    .build();

            // Send the request and get the response
            httpResponse = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

            // Parse the response
            if (httpResponse.statusCode() == 200) {
                response = jacksonObjectMapper.readValue(httpResponse.body(),
                        new TypeReference<DefaultKoraResponse<BankTransferResponseDTO>>() {});

                log.info(response.toString());
                log.info("Bank transfer initiated successfully for Employer with email {}", employer.getEmailAddress());

            } else {
                log.info(response.toString());

                response.setStatus(false);
                response.setMessage("Error Initiating Bank Transfer: ");
            }
            return response;
        } catch (Exception e) {
            log.error("Error while initiating bank transfer: {}", e.getMessage());
            throw new RuntimeException(e.getMessage());
        }
    }

    private Map<String, Object> createRequestBodyForTransfer(BigDecimal amount, String currency, Employer employer) {
        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("account_name", String.format("%s %s", employer.getFirstName(), employer.getLastName()));
        requestBody.put("amount", amount);
        requestBody.put("currency", currency);
        requestBody.put("reference", generateRef());  // Unique reference for each transaction
        requestBody.put("merchant_bears_cost", false);
        requestBody.put("notification_url", WEBHOOK_URL);  // webhook URL

        // Customer information
        Map<String, String> customer = new HashMap<>();
        customer.put("email", employer.getEmailAddress());
        requestBody.put("customer", customer);
        return requestBody;
    }

    /**
     * Generates Payment Reference for Customer
     */
    private String generateRef() {
        log.info("Generating Reference");
        String transactionReference;
        transactionReference = UUID.randomUUID().toString().substring(0, 12).replace("-", "");
        return transactionReference;
    }


    // ================ PAYOUT RELATED OPTIONS ============

    @Override
    public DefaultKoraResponse<List<BankTypeDTO>> listBanks() throws Exception {
        DefaultKoraResponse<List<BankTypeDTO>> response = new DefaultKoraResponse<>();

        // Create HTTP GET request to fetch all available bank codes for Nigeria
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create( BASE_URL + "misc/banks?countryCode=NG"))
                .header("Authorization", "Bearer " + PUBLIC_KEY)
                .header("Content-Type", "application/json")
                .GET()
                .build();

        HttpResponse<String> httpResponse;
        try {
            // Send the request and receive the response
            httpResponse = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
        } catch (IOException | InterruptedException e) {
            throw new RuntimeException(e); // Log and rethrow any errors
        }

        if (httpResponse.statusCode() == 200) {
            // Parse the response body to List<BankTypeDTO> if status code is 200 (OK)
            response = jacksonObjectMapper.readValue(httpResponse.body(),
                    new TypeReference<DefaultKoraResponse<List<BankTypeDTO>>>() {});

            log.info("Request for all available bank codes successful: Nigeria Only");
        } else {
            response.setStatus(false);  // Set status to false in case of failure
            response.setMessage("Error Initiating Bank Transfer: ");
        }

        return response;  // Return the final response
    }

    @Override
    public DefaultKoraResponse<BankAccountDTO> resolveBankAccount(String bankCode, String accountNumber) throws Exception {
        DefaultKoraResponse<BankAccountDTO> response = new DefaultKoraResponse<>();

        // Prepare request payload (bankCode and accountNumber)
        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("bank", bankCode);
        requestBody.put("account", accountNumber);

        // Convert request body to JSON format
        String requestBodyJson = jacksonObjectMapper.writeValueAsString(requestBody);

        // Create HTTP POST request to resolve bank account details
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(BASE_URL + "misc/banks/resolve"))
                .header("Authorization", "Bearer " + PUBLIC_KEY)
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(requestBodyJson))
                .build();

        HttpResponse<String> httpResponse;
        try {
            // Send the request and capture the response
            httpResponse = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
        } catch (IOException | InterruptedException e) {
            throw new RuntimeException(e);
        }

        if (httpResponse.statusCode() == 200) {
            // Deserialize the response body into BankTypeDTO if successful
            response = jacksonObjectMapper.readValue(httpResponse.body(),
                    new TypeReference<DefaultKoraResponse<BankAccountDTO>>() {});

            log.info("Bank account resolved: {}", response.getData());
        } else {
            response.setStatus(false);  // Set error status if request fails
            response.setMessage("Error Initiating Bank Transfer: ");
        }

        return response;  // Return the resolved bank account details
    }

    @Override
    public DefaultKoraResponse<PayoutResponseDTO> requestPayout(String bankCode, String accountNumber, BigDecimal amount, Employer employer) throws Exception {
        DefaultKoraResponse<PayoutResponseDTO> response = new DefaultKoraResponse<>();

        // Generate the request body for a payout
        Map<String, Object> requestBody = generatePayoutRequestBody(amount, employer, bankCode, accountNumber, "NGN");

        // Convert the request body to JSON format
        String requestBodyJson = jacksonObjectMapper.writeValueAsString(requestBody);

        // Create HTTP POST request to initiate a payout
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(BASE_URL + "transactions/disburse"))
                .header("Authorization", "Bearer " + SECRET_KEY)
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(requestBodyJson))
                .build();

        HttpResponse<String> httpResponse;
        try {
            // Send the request and capture the response
            httpResponse = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
        } catch (IOException | InterruptedException e) {
            throw new RuntimeException(e);
        }

        if (httpResponse.statusCode() == 200) {
            // Deserialize the response into PayoutResponseDTO if successful
            response = jacksonObjectMapper.readValue(httpResponse.body(),
                    new TypeReference<DefaultKoraResponse<PayoutResponseDTO>>() {});

            log.info("Payout Request Successful: {}", response.getData());
        } else {
            response.setStatus(false);  // Set failure status in case of error
            response.setMessage("Error Initiating Bank Transfer: ");
        }

        return response;  // Return the payout response
    }

    @Override
    public DefaultKoraResponse<BulkPayoutResponseDTO> requestBulkPayout(List<PayoutData> payrollList, Employer employer) throws Exception {
        DefaultKoraResponse<BulkPayoutResponseDTO> response = new DefaultKoraResponse<>();

        // Generate the request body for a bulk payout
        Map<String, Object> requestBody = generateBulkPayoutRequestBody(payrollList, employer);

        // Convert the request body to JSON format
        String requestBodyJson = jacksonObjectMapper.writeValueAsString(requestBody);

        // Create HTTP POST request for bulk payouts
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(BASE_URL + "transactions/disburse/bulk"))
                .header("Authorization", "Bearer " + SECRET_KEY)
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(requestBodyJson))
                .build();

        HttpResponse<String> httpResponse;
        try {
            // Send the request and capture the response
            httpResponse = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
        } catch (IOException | InterruptedException e) {
            throw new RuntimeException(e);
        }

        if (httpResponse.statusCode() == 200) {
            // Deserialize the response body into BulkPayoutResponseDTO if successful
            response = jacksonObjectMapper.readValue(httpResponse.body(),
                    new TypeReference<DefaultKoraResponse<BulkPayoutResponseDTO>>() {});

            log.info("Bulk Payout Request Successful: {}", response.getData());
        } else {
            response.setStatus(false);  // Set failure status in case of error
            response.setMessage("Error Initiating Bank Transfer: ");
        }

        return response;  // Return the bulk payout response
    }

    // Helper method to generate the request body for a single payout

    private Map<String, Object> generatePayoutRequestBody(BigDecimal amount, Employer employer, String bankCode, String accountNumber, String currency) {
        // 044, 033, 058 - Allowed Bank Codes for Success

        HashMap<String, Object> requestBody = new HashMap<>();
        requestBody.put("reference", generateRef() + "-" + employer.getLastName().toLowerCase());

        // Create the 'destination' HashMap
        HashMap<String, Object> destination = new HashMap<>();
        destination.put("type", "bank_account");
        destination.put("amount", amount);
        destination.put("narration", "Salary Payment");

        // Optionally add currency if provided
        if (currency != null) {
            destination.put("currency", currency);
        }

        // Create the 'bank_account' HashMap
        HashMap<String, String> bankAccount = new HashMap<>();
        bankAccount.put("bank", bankCode);
        bankAccount.put("account", accountNumber);

        // Add the 'bank_account' to 'destination'
        destination.put("bank_account", bankAccount);

        // Create the 'customer' HashMap
        HashMap<String, String> customer = new HashMap<>();
        customer.put("email", employer.getEmailAddress());

        // Add the 'customer' to 'destination'
        destination.put("customer", customer);

        // Return the complete request body
        return requestBody;
    }

    // Helper method to generate the request body for a bulk payout
    private Map<String, Object> generateBulkPayoutRequestBody(List<PayoutData> payoutDataList, Employer employer) {
        HashMap<String, Object> requestBody = new HashMap<>();

        // Set the batch reference and description
        requestBody.put("batch_reference", generateRef());
        requestBody.put("description", "test bulk transfer");
        requestBody.put("merchant_bears_cost", true);
        requestBody.put("currency", "NGN");

        // Initialize the list of payouts
        List<Map<String, Object>> payouts = new ArrayList<>();

        // Loop through the payout data and generate each payout request
        for (PayoutData payoutData : payoutDataList) {
            Map<String, Object> payoutRequest = generatePayoutRequestBody(
                    payoutData.getAmount(),
                    employer,
                    payoutData.getBankCode(),
                    payoutData.getAccountNumber(),
                    null
            );
            payouts.add(payoutRequest);  // Add each payout request to the list
        }

        // Add the list of payouts to the request body
        requestBody.put("payouts", payouts);

        // Return the complete request body for bulk payout
        return requestBody;
    }

}