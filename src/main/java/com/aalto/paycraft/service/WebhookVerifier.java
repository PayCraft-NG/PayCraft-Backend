package com.aalto.paycraft.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.util.Base64;

@Component @Slf4j
public class WebhookVerifier {
    public static boolean verifySignature(String payload, String signature, String secretKey) {
        try {
            // Use HMAC-SHA256 to hash the payload using the secret key
            Mac sha256_HMAC = Mac.getInstance("HmacSHA256");
            SecretKeySpec secret_key = new SecretKeySpec(secretKey.getBytes(), "HmacSHA256");
            sha256_HMAC.init(secret_key);

            // Generate the hash and encode it in Base64
            byte[] hash = sha256_HMAC.doFinal(payload.getBytes());
            String generatedSignature = Base64.getEncoder().encodeToString(hash);

            // Compare the generated hash with the signature from Korapay
            return generatedSignature.equals(signature);
        } catch (Exception e) {
            log.error("An error occurred while verifying signature", e);
            return false;
        }
    }
}
