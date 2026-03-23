package com.aalto.paycraft.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
@JsonIgnoreProperties
@JsonInclude(JsonInclude.Include.NON_NULL)
@Schema(description = "API envelope: statusCode, statusMessage, and typed data payload")
public class DefaultApiResponse<T> {
    private String statusCode;
    private String statusMessage;
    private T data;
}