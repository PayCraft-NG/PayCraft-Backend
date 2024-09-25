package com.aalto.paycraft.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;

@Component
@Slf4j
public class WebhookVerifier {

    private static final char[] HEX_ARRAY = "0123456789abcdef".toCharArray();

    // Convert bytes to hex format
    private static String bytesToHex(byte[] bytes) {
        char[] hexChars = new char[bytes.length * 2];
        for (int j = 0; j < bytes.length; j++) {
            int v = bytes[j] & 0xFF;
            hexChars[j * 2] = HEX_ARRAY[v >> 4];
            hexChars[j * 2 + 1] = HEX_ARRAY[v & 0x0F];
        }
        return new String(hexChars);
    }

    // Method to verify the signature
    public static boolean verifySignature(String payload, String signature, String secretKey) {
        try {
            // Use HMAC-SHA256 to hash the payload using the secret key
            Mac sha256_HMAC = Mac.getInstance("HmacSHA256");
            SecretKeySpec secret_key = new SecretKeySpec(secretKey.getBytes(StandardCharsets.UTF_8), "HmacSHA256");
            sha256_HMAC.init(secret_key);

            // Generate the HMAC hash
            byte[] hash = sha256_HMAC.doFinal(payload.getBytes(StandardCharsets.UTF_8));
            String generatedSignature = bytesToHex(hash);

            // Compare the generated HMAC hex with the signature from Korapay
            return generatedSignature.equals(signature);
        } catch (Exception e) {
            log.error("An error occurred while verifying the signature", e);
            return false;
        }
    }
}
