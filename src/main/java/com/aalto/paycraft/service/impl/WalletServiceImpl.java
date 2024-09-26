package com.aalto.paycraft.service.impl;

import com.aalto.paycraft.dto.*;
import com.aalto.paycraft.dto.enums.Currency;
import com.aalto.paycraft.entity.Employer;
import com.aalto.paycraft.entity.VirtualAccount;
import com.aalto.paycraft.repository.VirtualAccountRepository;
import com.aalto.paycraft.service.IWalletService;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import javax.crypto.Cipher;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.math.BigDecimal;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.security.SecureRandom;
import java.util.*;

@Slf4j @Service @RequiredArgsConstructor
public class WalletServiceImpl implements IWalletService {

    private final HttpClient httpClient = HttpClient.newHttpClient();
    private final ObjectMapper jacksonObjectMapper;
    private final VirtualAccountRepository vAccountRepository;

    @Value("${kora-secret}")
    private String SECRET_KEY;

    @Value("${webhook-url}")
    private String WEBHOOK_URL;

    @Value("${encryption-key}")
    private String ENCRYPTION_KEY;

    private final String BASE_URL = "https://api.korapay.com/merchant/api/v1/";

    private static final String VBA = "/virtual-bank-account";
    private static final String FUND = "charges/bank-transfer";
    private static final String FUND_CARD = "charges/card";
    private static final String VBATransaction = "/virtual-bank-account/transactions";


    @Override
    public VirtualAccount createVirtualAccount(Employer employer) {
        VirtualAccount account = new VirtualAccount();

        // Preparing request body
        Map<String, Object> requestBody = createRequestBody(employer);

        try {
            // Convert request body to JSON
            String requestBodyJson = jacksonObjectMapper.writeValueAsString(requestBody);

            // Build the request
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(BASE_URL + VBA))
                    .header("Content-Type", "application/json")
                    .header("Authorization", "Bearer " + SECRET_KEY)
                    .POST(HttpRequest.BodyPublishers.ofString(requestBodyJson))
                    .build();

            // Send the request and get the response
            HttpResponse<String> httpResponse = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

            // Parse the response
            if (httpResponse.statusCode() == 200) {
                DefaultKoraResponse<VirtualAccountResponseDTO> responseBody =
                        jacksonObjectMapper.readValue(httpResponse.body(),
                        new TypeReference<DefaultKoraResponse<VirtualAccountResponseDTO>>() {});
                log.info("Created virtual account for Employer with email {} successfully", employer.getEmailAddress());

                log.info(httpResponse.body());
                log.info(responseBody.toString());

                if (responseBody.getData() != null) {
                    VirtualAccountResponseDTO response = responseBody.getData();

                    account = VirtualAccount.builder()
                            .accountNumber(response.getAccount_number())
                            .accountReference(response.getAccount_reference())
                            .koraAccountReference(response.getUnique_id())
                            .bankCode(response.getBank_code())
                            .bankName(response.getBank_name())
                            .accountStatus(response.getAccount_status())
                            .balance(BigDecimal.ZERO)
                            .currency(Currency.valueOf(response.getCurrency()))
                            .employer(employer)
                            .build();
                }else {
                    log.error("Failed to create virtual bank account because data is null: {}", httpResponse.body());
                }

                vAccountRepository.save(account);
            } else {
                log.error("Failed to create virtual bank account: {}", httpResponse.body());
            }
        } catch (Exception e) {
            log.error("Error while creating virtual bank account {}", e.getMessage());
        }

        return account;
    }

    // New method to initiate a bank transfer
    @Override
    public DefaultKoraResponse<BankTransferResponseDTO> initiateBankTransfer(BigDecimal amount, String currency, Employer employer) {
        HttpResponse<String> httpResponse = null;
        DefaultKoraResponse<BankTransferResponseDTO> response = null;
        try {
            // Prepare request body
            Map<String, Object> requestBody = createRequestBodyForTransfer(amount, currency, employer);

            // Convert request body to JSON
            String requestBodyJson = jacksonObjectMapper.writeValueAsString(requestBody);
            String url = BASE_URL + FUND;

            // Build the request
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(url))
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
                return ResponseEntity.status(HttpStatus.OK).body(response).getBody();
            } else {
                log.error("Failed to initiate bank transfer: {}", httpResponse.body());
            }
        } catch (Exception e) {
            log.error("Error while initiating bank transfer: {}", e.getMessage());
        }

        return ResponseEntity.status(HttpStatus.OK).body(response).getBody();
    }


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
    public DefaultApiResponse<?> chargeCard(CardFundingRequestDTO payload, Employer employer) throws Exception {
        DefaultKoraResponse<PaymentDataDTO> response = new DefaultKoraResponse<>();
        try{
            Map<String, Object> requestBody = createCardFundingRequestBody(payload, employer);


            // Convert request body to JSON
            String requestBodyJson = jacksonObjectMapper.writeValueAsString(requestBody);
            String url = BASE_URL + FUND_CARD;

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
                    if(response.getData().getAuth_model().equals("OTP")){
                        authorizeTransactionWithOtp("12345", response.getData().getTransaction_reference());
                    }
                }else if(response.getData().getStatus().equals("success")){
                    boolean isValid = verifyPayment(response.getData().getTransaction_reference());
                    if(isValid){
                        log.info("Success on Funding Card");
                        DefaultApiResponse<?> apiResponse = new DefaultApiResponse<>();
                        apiResponse.setStatusCode("00");
                        apiResponse.setStatusMessage("Account Credited Successfully");
                    }
                }
            } else {
                log.error("Failed to initiate Card Funding: {}", response.getData());
            }
        }catch (Exception ex){
            log.error(ex.getMessage());
        }
        return null;
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

    private Map<String, Object> createRequestBodyForTransfer(BigDecimal amount, String currency, Employer employer) {
        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("account_name", String.format("%s %s", employer.getFirstName(), employer.getLastName()));
        requestBody.put("amount", amount);
        requestBody.put("currency", currency);
        requestBody.put("reference", UUID.randomUUID().toString());  // Unique reference for each transaction
        requestBody.put("merchant_bears_cost", false);
        requestBody.put("notification_url", WEBHOOK_URL);  // webhook URL

        // Customer information
        Map<String, String> customer = new HashMap<>();
        customer.put("name", String.format("%s %s", employer.getFirstName(), employer.getLastName()));
        customer.put("email", employer.getEmailAddress());
        requestBody.put("customer", customer);

        return requestBody;
    }

    private static Map<String, Object> createRequestBody(Employer employer) {
        Map<String, Object> requestBody = new HashMap<>();
        String fullName = String.format("%s %s", employer.getFirstName(), employer.getLastName());
        requestBody.put("account_name", fullName);
        requestBody.put("account_reference", employer.getEmployerId() + UUID.randomUUID().toString().substring(0,4));
        requestBody.put("permanent", true);
        requestBody.put("bank_code", "000");

        // Customer information
        Map<String, String> customer = new HashMap<>();
        customer.put("name",fullName);
        customer.put("email", employer.getEmailAddress());
        requestBody.put("customer", customer);

        // KYC information
        Map<String, String> kyc = new HashMap<>();
        kyc.put("bvn", employer.getBvn());
        requestBody.put("kyc", kyc);
        return requestBody;
    }

    private Map<String, Object> createCardFundingRequestBody(CardFundingRequestDTO requestDTO, Employer employer){
        // Create the main request body map
        Map<String, Object> requestBody = new HashMap<>();

        // Add reference
        requestBody.put("reference", generateTransactionRef());

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


    /** Generates Transaction Reference for Customer */
    private String generateTransactionRef() {
        log.info("Generating transaction ref for transactions");
        String transactionReference;
        transactionReference = UUID.randomUUID().toString().substring(0,12).replace("-","");
        return transactionReference;
    }
}
