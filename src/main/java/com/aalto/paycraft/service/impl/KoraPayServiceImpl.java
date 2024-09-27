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
            StringBuilder urlBuilder = new StringBuilder(BASE_URL + "/virtual-bank-account/transactions?account_number=" + accountNumber);

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

            log.info("Sending request to fetch VBA transactions for employer: {}", employer.getEmailAddress());

            // Send the request and capture the response
            HttpResponse<String> httpResponse = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

            // Handle successful response
            if (httpResponse.statusCode() == 200) {
                response = jacksonObjectMapper.readValue(httpResponse.body(),
                        new TypeReference<DefaultKoraResponse<VBATransactionDTO>>() {
                        });
                log.info("VBA Transactions fetched successfully for employer: {}", employer.getEmailAddress());
            } else {
                // Handle non-200 response from the API
                response.setStatus(false);
                response.setMessage("Error Fetching Transactions: " + httpResponse.body());
                log.error("Error Fetching Transactions: {}", httpResponse.body());
            }
        } catch (Exception e) {
            // Handle any exceptions during the process
            log.error("Error Fetching Transactions for employer {}: {}", employer.getEmailAddress(), e.getMessage());
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
                log.info("Bank transfer initiated successfully for Employer with email {}", employer.getEmailAddress());

            } else {
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
     * Generates Transaction Reference for Customer
     */
    private String generateRef() {
        log.info("Generating Reference");
        String transactionReference;
        transactionReference = UUID.randomUUID().toString().substring(0, 12).replace("-", "");
        return transactionReference;
    }
}