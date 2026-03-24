package com.aalto.paycraft.service.impl;

import com.aalto.paycraft.service.IEmailService;
import com.resend.Resend;
import com.resend.core.exception.ResendException;
import com.resend.services.emails.model.CreateEmailOptions;
import com.resend.services.emails.model.CreateEmailResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

@Service
@Slf4j
@RequiredArgsConstructor
public class EmailServiceImpl implements IEmailService {
    private final TemplateEngine templateEngine;

    @Value("${resend.from-email}")
    private String senderEmail;

    @Value("${resend.api-key}")
    private String resendApiKey;

    @Async
    @Override
    public void sendEmail(String toEmail, String subject, Context context, String template) {
        int maxRetries = 3;
        int retryCount = 0;
        long retryDelay = 1000; // 1 second

        String htmlContent = templateEngine.process(template, context);
        //noinspection ConstantValue
        while (retryCount < maxRetries) {
            try {
                if (resendApiKey == null || resendApiKey.isBlank()) {
                    throw new IllegalStateException("RESEND_API_KEY is not configured");
                }
                if (senderEmail == null || senderEmail.isBlank()) {
                    throw new IllegalStateException("EMAIL_SENDER (resend.from-email) is not configured");
                }

                Resend resend = new Resend(resendApiKey);
                CreateEmailOptions params = CreateEmailOptions.builder()
                        .from(senderEmail)
                        .to(toEmail)
                        .subject(subject)
                        .html(htmlContent)
                        .build();
                CreateEmailResponse response = resend.emails().send(params);

                /* If successful, break out of the retry loop */
                log.info("Email sent successfully with Resend id={}", response.getId());
                return;
            } catch (ResendException | RuntimeException e) {
                retryCount++;
                log.info("Attempt {} failed. Error: {}", retryCount, e.getMessage());

                if (retryCount >= maxRetries) {
                    log.info("All retry attempts failed. Giving up.");
                    throw new RuntimeException("Failed to send email after " + maxRetries + " attempts", e);
                }

                try {
                    Thread.sleep(retryDelay);
                } catch (InterruptedException ie) {
                    Thread.currentThread().interrupt();
                    throw new RuntimeException("Retry interrupted", ie);
                }
            }
        }
    }
}
