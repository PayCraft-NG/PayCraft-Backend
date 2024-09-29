package com.aalto.paycraft.service.impl;

import com.aalto.paycraft.service.IEmailService;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

@Service
@Slf4j
@RequiredArgsConstructor
public class EmailServiceImpl implements IEmailService {
    private final JavaMailSender mailSender;
    private  final TemplateEngine templateEngine;

    @Value("${spring.mail.username}")
    private String senderEmail;

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
                MimeMessage message = mailSender.createMimeMessage();
                MimeMessageHelper helper = new MimeMessageHelper(message, true);
                helper.setFrom(senderEmail);
                helper.setTo(toEmail);
                helper.setSubject(subject);
                helper.setText(htmlContent, true);

                mailSender.send(message);

                /* If successful, break out of the retry loop */
                log.info("Email sent successfully");
                return;
            } catch (Exception e) {
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
