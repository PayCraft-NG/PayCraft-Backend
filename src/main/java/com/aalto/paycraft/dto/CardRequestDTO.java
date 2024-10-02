package com.aalto.paycraft.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import jakarta.validation.constraints.NotNull;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Data;

@Data @Builder
@JsonIgnoreProperties
@JsonInclude(JsonInclude.Include.NON_NULL)
public class CardRequestDTO {

    @NotNull(message = "Card Number cannot be empty")
    @Schema(description = "The card number", example = "4084127883172787")
    private String cardNumber;

    @NotNull(message = "Expiry Month cannot be empty")
    @Schema(description = "The card's expiry month in two digits", example = "09")
    private String expiryMonth;

    @NotNull(message = "Expiry year cannot be empty")
    @Schema(description = "The card's expiry year in two digits", example = "23")
    private String expiryYear;

    @NotNull(message = "CVV cannot be empty")
    @Schema(description = "The CVV security code of the card", example = "123")
    private String cvv;

    @NotNull(message = "Card Pin cannot be empty")
    @Schema(description = "The card's PIN", example = "1234")
    private String cardPin;

    private long cardId;
}
