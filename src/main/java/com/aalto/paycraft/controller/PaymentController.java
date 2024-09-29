package com.aalto.paycraft.controller;

import com.aalto.paycraft.dto.DefaultApiResponse;
import com.aalto.paycraft.dto.PaymentDTO;
import com.aalto.paycraft.service.IPaymentService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@Tag(
        name = "Payment Service Controller",
        description = "CRUD REST APIs to CREATE, READ, UPDATE Virtual Account details"
)
@RestController
@RequiredArgsConstructor
@RequestMapping(value = "api/v1/account/", produces = MediaType.APPLICATION_JSON_VALUE)
public class PaymentController {

    private final IPaymentService paymentService;

    @PostMapping("/pay")
    public ResponseEntity<DefaultApiResponse<?>> payEmployee(@RequestParam String employeeId){
        return ResponseEntity.status(HttpStatus.OK)
                .body(paymentService.payEmployee(UUID.fromString(employeeId)));
    }

    @GetMapping("/banks")
    public ResponseEntity<DefaultApiResponse<List<String>>> getBanks() throws Exception {
        return ResponseEntity.status(HttpStatus.OK).body(
                paymentService.getBankNames()
        );
    }

    /**
     * Endpoint for verifying a bank transfer using a reference number.
     *
     * @param referenceNumber The reference number of the bank transfer.
     * @return A response entity indicating the status of the bank transfer verification.
     */
    @Operation(summary = "Verifies the Bank Transfer Made")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Bank transfer successful"),
            @ApiResponse(responseCode = "400", description = "Bank transfer failed")
    })
    @GetMapping("/verify-pay/{referenceNumber}") // GET request for verifying bank transfer
    public ResponseEntity<DefaultApiResponse<?>> verifyPayment(
            @PathVariable String referenceNumber) {
        DefaultApiResponse<?> response = paymentService.verifyPayment(referenceNumber);
        return ResponseEntity.status(HttpStatus.OK).body(response);
    }

}
