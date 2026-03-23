package com.aalto.paycraft.controller;

import com.aalto.paycraft.dto.BulkPayoutResponseDTO;
import com.aalto.paycraft.dto.DefaultApiResponse;
import com.aalto.paycraft.service.IPaymentService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@Tag(
        name = "Payments (payouts & banks)",
        description = "Single and bulk employee payouts, bank list, and payment verification."
)
@SecurityRequirement(name = "bearerAuth")
@RestController
@RequiredArgsConstructor
@RequestMapping(value = "api/v1/account/", produces = MediaType.APPLICATION_JSON_VALUE)
public class PaymentController {

    private final IPaymentService paymentService;

    @PostMapping("/pay")
    @Operation(summary = "Pay single employee",
            description = "Trigger payout for one employee (UUID).")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Payment processed or status returned"),
            @ApiResponse(responseCode = "400", description = "Invalid employee ID or business error"),
            @ApiResponse(responseCode = "401", description = "Missing or invalid JWT")
    })
    public ResponseEntity<DefaultApiResponse<?>> payEmployee(
            @Parameter(description = "Employee UUID", required = true) @RequestParam String employeeId) {
        return ResponseEntity.status(HttpStatus.OK)
                .body(paymentService.payEmployee(UUID.fromString(employeeId)));
    }

    @PostMapping("/pay/bulk")
    @Operation(summary = "Bulk pay payroll",
            description = "Process payouts for all employees on a payroll run.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Bulk payout result"),
            @ApiResponse(responseCode = "400", description = "Invalid payroll ID or business error"),
            @ApiResponse(responseCode = "401", description = "Missing or invalid JWT")
    })
    public ResponseEntity<DefaultApiResponse<BulkPayoutResponseDTO>> payBulkEmployees(
            @Parameter(description = "Payroll UUID", required = true) @RequestParam String payrollId) {
        return ResponseEntity.status(HttpStatus.OK)
                .body(paymentService.payEmployeesBulk(UUID.fromString(payrollId)));
    }

    @GetMapping("/banks")
    @Operation(summary = "List banks",
            description = "Supported bank names for payouts / transfers.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Bank names retrieved"),
            @ApiResponse(responseCode = "401", description = "Missing or invalid JWT"),
            @ApiResponse(responseCode = "500", description = "Upstream or internal error")
    })
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
    @Operation(summary = "Verify payment by reference",
            description = "Check status of a transfer using its reference number.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Verification result"),
            @ApiResponse(responseCode = "400", description = "Invalid reference or failed verification"),
            @ApiResponse(responseCode = "401", description = "Missing or invalid JWT")
    })
    @GetMapping("/verify-pay/{referenceNumber}")
    public ResponseEntity<DefaultApiResponse<?>> verifyPayment(
            @Parameter(description = "Payment / transfer reference", required = true)
            @PathVariable String referenceNumber) {
        DefaultApiResponse<?> response = paymentService.verifyPayment(referenceNumber);
        return ResponseEntity.status(HttpStatus.OK).body(response);
    }

}
