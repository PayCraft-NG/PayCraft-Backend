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

import javax.crypto.Cipher;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.io.IOException;
import java.math.BigDecimal;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.security.SecureRandom;
import java.util.*;

import static com.aalto.paycraft.constants.PayCraftConstant.REQUEST_SUCCESS;

@Slf4j
@Service
@RequiredArgsConstructor
public class KoraPayServiceImpl implements IKoraPayService {

    @Value("${kora-secret}")
    private String SECRET_KEY;

    @Value("${webhook-dev-url}")
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
        log.info(WEBHOOK_URL);
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

                log.info("Bank transfer SUCCESS {}",httpResponse);
                log.info("Bank transfer initiated successfully for Employer with email {}", employer.getEmailAddress());

            } else {
                log.info("Bank transfer FAILED {}" ,httpResponse);

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

        log.info(accountNumber);
        // Prepare request payload (bankCode and accountNumber)
        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("bank", bankCode);
        requestBody.put("account", accountNumber);

        // Convert request body to JSON format
        String requestBodyJson = jacksonObjectMapper.writeValueAsString(requestBody);
        log.info(requestBodyJson);

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

            log.info("Bank account resolved: {}", response);
        } else {
            log.info(response.toString());

            response.setStatus(false);  // Set error status if request fails
            response.setMessage("Error Resolving Bank Account ");
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

        log.info(httpResponse.body());

        if (httpResponse.statusCode() == 200) {
            // Deserialize the response into PayoutResponseDTO if successful
            response = jacksonObjectMapper.readValue(httpResponse.body(),
                    new TypeReference<DefaultKoraResponse<PayoutResponseDTO>>() {});

            log.info("Payout Request Successful: {}", httpResponse.toString());
        } else {
            log.info("Payout Request FAILED: {}", httpResponse);

            response.setStatus(false);  // Set failure status in case of error
            response.setMessage("Error making payout request: {}");
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

            log.info("Bulk Payout Request Successful: {}", httpResponse.body());
        } else {
            log.info("Bulk Payout Request FAILED: {}", httpResponse.body());
            response.setStatus(false);  // Set failure status in case of error
            response.setMessage("Error Initiating Bank Transfer: " + httpResponse.body());
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

        requestBody.put("destination", destination);
        // Return the complete request body
        return requestBody;
    }

    private Map<String, Object> generateBulkPayoutRequestBody(List<PayoutData> payoutDataList, Employer employer) {
        HashMap<String, Object> requestBody = new HashMap<>();

        // Set the batch reference and description
        requestBody.put("batch_reference", generateRef());
        requestBody.put("description", "test bulk transfer");
        requestBody.put("merchant_bears_cost", true);  // Set as true to bear the cost, false otherwise
        requestBody.put("currency", "NGN");  // Set the currency, e.g., "NGN"

        // Initialize the list of payouts
        List<Map<String, Object>> payouts = new ArrayList<>();

        // Loop through the payout data and generate each payout request
        for (PayoutData payoutData : payoutDataList) {
            Map<String, Object> payoutRequest = new HashMap<>();

            // Payout reference
            payoutRequest.put("reference", generateRef());  // Ensure unique reference for each payout

            // Payout amount
            payoutRequest.put("amount", payoutData.getAmount());

            // Payout type (bank_account in this case)
            payoutRequest.put("type", "bank_account");

            // Narration
            payoutRequest.put("narration", "Bulk payout to " + payoutData.getFullName());

            // Bank account details
            Map<String, Object> bankAccount = new HashMap<>();
            bankAccount.put("bank_code", payoutData.getBankCode());
            bankAccount.put("account_number", payoutData.getAccountNumber());
            payoutRequest.put("bank_account", bankAccount);

            // Customer details
            Map<String, Object> customer = new HashMap<>();
            customer.put("name", payoutData.getFullName());
            customer.put("email", payoutData.getEmail());
            payoutRequest.put("customer", customer);

            // Add the payout request to the list
            payouts.add(payoutRequest);
        }

        // Add the list of payouts to the request body (note the correct key is "payouts")
        requestBody.put("payouts", payouts);

        // Return the complete request body for bulk payout
        return requestBody;
    }

    // ========== INITIATE CARD PAYMENT =========
    private static final char[] HEX_ARRAY = "0123456789abcdef".toCharArray();

    private static String bytesToHex(byte[] bytes) {
        char[] hexChars = new char[bytes.length * 2];
        for (int j = 0; j < bytes.length; j++) {
            int v = bytes[j] & 0xFF;
            hexChars[j * 2] = HEX_ARRAY[v >>> 4];
            hexChars[j * 2 + 1] = HEX_ARRAY[v & 0x0F];
        }
        return new String(hexChars);
    }

    private static byte[] encryptDataWithAes(byte[] plainText, byte[] aesKey, byte[] aesIv) throws Exception {
        GCMParameterSpec gcmSpec = new GCMParameterSpec(128, aesIv);
        Cipher cipher = Cipher.getInstance("AES/GCM/NoPadding");
        SecretKeySpec secretKeySpec = new SecretKeySpec(aesKey, "AES");
        cipher.init(Cipher.ENCRYPT_MODE, secretKeySpec, gcmSpec);
        byte[] cipherText = cipher.doFinal(plainText);

        return cipherText;
    }

    public String encryptPayload(String payload) throws Exception {
        SecureRandom r = new SecureRandom();

        byte[] ivBytes = new byte[16];
        r.nextBytes(ivBytes);

        byte[] keyBytes   = ENCRYPTION_KEY.getBytes(StandardCharsets.UTF_8);
        byte[] inputBytes = payload.getBytes(StandardCharsets.UTF_8);
        byte[] encryptedBytes = encryptDataWithAes(inputBytes, keyBytes, ivBytes);

        byte[] cipherTextBytes = Arrays.copyOfRange(encryptedBytes, 0, payload.length());
        byte[] authTagBytes = Arrays.copyOfRange(encryptedBytes, payload.length(), encryptedBytes.length);

        String ivHex = bytesToHex(ivBytes);
        String encryptedHex = bytesToHex(cipherTextBytes);
        String authTagHex = bytesToHex(authTagBytes);

        return ivHex + ":" + encryptedHex + ":" + authTagHex;
    }

    @Override
    public DefaultKoraResponse<PaymentDataDTO> chargeCard(CardFundingRequestDTO payload, Employer employer) throws Exception {
        DefaultKoraResponse<PaymentDataDTO> response = new DefaultKoraResponse<>();
        try{
            Map<String, Object> requestBody = createCardFundingRequestBody(payload, employer);


            // Convert request body to JSON
            String requestBodyJson = jacksonObjectMapper.writeValueAsString(requestBody);
            String url = BASE_URL + "charges/card";;

            Map<String, String> mainRequest = new HashMap<>();
            mainRequest.put("charge_data", encryptPayload(requestBodyJson));

            String mainRequestJson = jacksonObjectMapper.writeValueAsString(mainRequest);

            // Build the request
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(url))
                    .header("Content-Type", "application/json")
                    .header("Authorization", "Bearer " + SECRET_KEY)
                    .POST(HttpRequest.BodyPublishers.ofString(mainRequestJson))
                    .build();

            // Send the request and get the response
            HttpResponse<String> httpResponse = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            log.info(httpResponse.body());

            // Parse the response
            if (httpResponse.statusCode() == 200) {
                response = jacksonObjectMapper.readValue(httpResponse.body(),
                        new TypeReference<DefaultKoraResponse<PaymentDataDTO>>() {});
                log.info("Card Funding initiated successfully for Employer with email {}", employer.getEmailAddress());

                if(response.getData().getStatus().equals("processing")){
                    log.info("Response Received Success: {}", response);
                    if(response.getData().getAuth_model().equals("OTP")){
                        authorizeTransactionWithOtp("12345", response.getData().getTransaction_reference());
                    }
                }else if(response.getData().getStatus().equals("success")){
                    boolean isValid = verifyPayment(response.getData().getTransaction_reference());
                    log.info("Response Received FAILED: {}", response);
                    if(isValid){
                        log.info("Success on Funding Card");
                        response.setStatus(true);
                        response.setMessage("Account Credited Successfully");
                    }
                }
            } else {
                response.setStatus(false);
                response.setMessage("Card Funding Failed: " + response);
                log.error("Failed to initiate Card Funding: {}", response);
            }
        }catch (Exception ex){
            log.error(ex.getMessage());
            response.setStatus(false);
            response.setMessage("Card Funding Failed: " + ex.getMessage());
        }

        return response;
    }

    private boolean verifyPayment(String paymentReference) throws Exception {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(BASE_URL + "/charges/" + paymentReference))
                .header("Content-Type", "application/json")
                .header("Authorization", "Bearer " + SECRET_KEY).GET().build();

        // Send the request and get the response
        HttpResponse<String> httpResponse = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
        DefaultKoraResponse<VerifyPaymentDTO> response = jacksonObjectMapper.readValue(httpResponse.body(),
                new TypeReference<DefaultKoraResponse<VerifyPaymentDTO>>() {});
        if (httpResponse.statusCode() == 200) {
            return response.getData().getStatus().equals("success");
        } else {
            log.error("Failed to verify payment: {}", response.getData());
        }
        return false;
    }

    private void authorizeTransactionWithOtp(String otp, String transactionReference) throws Exception {

        Map<String, Object> requestBody = new HashMap<>();
        Map<String, String> authorization = new HashMap<>();

        authorization.put("otp", otp);
        requestBody.put("transaction_reference", transactionReference);
        requestBody.put("authorization", authorization);

        String requestBodyJson = jacksonObjectMapper.writeValueAsString(requestBody);

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(BASE_URL + "/charges/authorize"))
                .header("Content-Type", "application/json")
                .header("Authorization", "Bearer " + SECRET_KEY)
                .POST(HttpRequest.BodyPublishers.ofString(requestBodyJson))
                .build();

        // Send the request and get the response
        HttpResponse<String> httpResponse = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

        // Parse the response
        if (httpResponse.statusCode() == 200) {
            log.info("Processed Card with OTP");
        }
    }

    private Map<String, Object> createCardFundingRequestBody(CardFundingRequestDTO requestDTO, Employer employer){
        // Create the main request body map
        Map<String, Object> requestBody = new HashMap<>();

        // Add reference
        requestBody.put("reference", generateRef());

        // Create card details map
        Map<String, Object> card = new HashMap<>();
        card.put("number", requestDTO.getCardNumber());
        card.put("cvv", requestDTO.getCvv());
        card.put("expiry_month", requestDTO.getExpiryMonth());
        card.put("expiry_year", requestDTO.getExpiryYear());
        card.put("pin", requestDTO.getCardPin()); // optional

        // Add card details to the request body
        requestBody.put("card", card);

        // Add amount and currency
        requestBody.put("amount", requestDTO.getAmount());
        requestBody.put("currency", "NGN");

        // Add redirect URL
//        requestBody.put("redirect_url", WEBHOOK_URL);

        // Create customer details map
        Map<String, Object> customerDetails = new HashMap<>();
        customerDetails.put("name", String.format("%s %s", employer.getFirstName(), employer.getLastName()));
        customerDetails.put("email", employer.getEmailAddress());

        // Add customer details to the request body
        requestBody.put("customer", customerDetails);
        return requestBody;
    }

}