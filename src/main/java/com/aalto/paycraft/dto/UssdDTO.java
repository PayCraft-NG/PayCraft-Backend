package com.aalto.paycraft.dto;

import io.swagger.v3.oas.annotations.Hidden;
import lombok.*;

@Hidden
@Data
@AllArgsConstructor
@NoArgsConstructor
public class UssdDTO {
    private String sessionId;
    private String serviceCode;
    private String phoneNumber;
    private String text;
}