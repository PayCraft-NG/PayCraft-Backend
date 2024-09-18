package com.aalto.paycraft.dto;

import lombok.*;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class UssdDTO {
    private String sessionId;
    private String serviceCode;
    private String phoneNumber;
    private String text;
}