package com.aalto.paycraft.controller;

import com.aalto.paycraft.service.KoraPayWebhook;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequiredArgsConstructor
@RequestMapping(produces = MediaType.APPLICATION_JSON_VALUE)
public class KoraPayWebhookController {

    private final KoraPayWebhook webhookService;

    @PostMapping("/webhook/korapay")
    public ResponseEntity<String> handleKoraPayWebHook(
            @RequestBody Map<String, String> payload,
            @RequestHeader("x-kora-signature") String signature) {
        return webhookService.verifyWebHook(payload, signature);
    }
}
