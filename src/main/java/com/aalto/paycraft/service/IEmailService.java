package com.aalto.paycraft.service;

import org.thymeleaf.context.Context;

public interface IEmailService {
    void sendEmail(String toEmail, String subject, Context context, String template);
}
