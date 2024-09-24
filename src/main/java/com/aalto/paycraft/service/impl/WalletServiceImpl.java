package com.aalto.paycraft.service.impl;

import com.aalto.paycraft.dto.BankTransferResponseDTO;
import com.aalto.paycraft.dto.DefaultKoraResponse;
import com.aalto.paycraft.dto.VirtualAccountResponseDTO;
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

import java.math.BigDecimal;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.HashMap;
import java.util.Map;

@Slf4j @Service @RequiredArgsConstructor
public class WalletServiceImpl implements IWalletService {

    private final HttpClient httpClient = HttpClient.newHttpClient();
    private final ObjectMapper jacksonObjectMapper;
    private final VirtualAccountRepository vAccountRepository;

    @Value("${kora-secret}")
    private String SECRET_KEY;

    private final String BASE_URL = "https://api.korapay.com/merchant/api/v1/";
    private static final String VBA = "/virtual-bank-account";
    private static final String FUND = "charges/bank-transfer";
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
            log.info(url);

            // Build the request
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(url))
                    .header("Content-Type", "application/json")
                    .header("Authorization", "Bearer " + SECRET_KEY)
                    .POST(HttpRequest.BodyPublishers.ofString(requestBodyJson))
                    .build();

            // Send the request and get the response
            httpResponse = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            log.info(httpResponse.body());

            // Parse the response
            if (httpResponse.statusCode() == 200) {
                response = jacksonObjectMapper.readValue(httpResponse.body(),
                        new TypeReference<DefaultKoraResponse<BankTransferResponseDTO>>() {});
                log.info("Bank transfer initiated successfully for Employer with email {}", employer.getEmailAddress());

                log.info(response.toString());
                log.info(httpResponse.body());

                return ResponseEntity.status(HttpStatus.OK).body(response).getBody();
            } else {
                log.error("Failed to initiate bank transfer: {}", httpResponse.body().substring(0, 40));
            }
        } catch (Exception e) {
            log.error("Error while initiating bank transfer: {}", e.getMessage());
        }

        return ResponseEntity.status(HttpStatus.OK).body(response).getBody();
    }

    private static Map<String, Object> createRequestBodyForTransfer(BigDecimal amount, String currency, Employer employer) {
        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("account_name", employer.getLastName());
        requestBody.put("amount", amount);
        requestBody.put("currency", currency);
        requestBody.put("reference", employer.getVirtualAccount().getAccountReference());  // Unique reference for each transaction
        requestBody.put("notification_url", "http://localhost:6020/webhook/korapay");  // webhook URL

        // Customer information
        Map<String, String> customer = new HashMap<>();
        customer.put("name", String.format("%s %s", employer.getFirstName(), employer.getLastName()));
        customer.put("email", employer.getEmailAddress());
        requestBody.put("customer", customer);

        log.info(requestBody.toString());
        return requestBody;
    }

    private static Map<String, Object> createRequestBody(Employer employer) {
        Map<String, Object> requestBody = new HashMap<>();
        String fullName = String.format("%s %s", employer.getFirstName(), employer.getLastName());
        requestBody.put("account_name", fullName);
        requestBody.put("account_reference", employer.getEmployerId());
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
}
