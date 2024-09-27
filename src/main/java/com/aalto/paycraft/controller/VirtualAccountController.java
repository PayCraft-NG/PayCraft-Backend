package com.aalto.paycraft.controller;

import com.aalto.paycraft.dto.BankTransferDetailsDTO;
import com.aalto.paycraft.dto.DefaultApiResponse;
import com.aalto.paycraft.dto.VirtualAccountDTO;
import com.aalto.paycraft.dto.VirtualAccountTransactionDTO;
import com.aalto.paycraft.service.IVirtualAccountService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;

@Tag(
        name = "Virtual Account Controller",
        description = "CRUD REST APIs to CREATE, READ, UPDATE Virtual Account details"
)
@RestController
@RequiredArgsConstructor
@RequestMapping(value = "api/v1/account", produces = MediaType.APPLICATION_JSON_VALUE)
public class VirtualAccountController {

    private final IVirtualAccountService virtualAccountService;

    /**
     * Endpoint to create a virtual account
     * @return ResponseEntity with the created virtual account details
     */
    @Operation(summary = "Create Virtual Account for Employer")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Virtual bank account created successfully"),
            @ApiResponse(responseCode = "400", description = "Unable to create virtual account")
    })
    @PostMapping("/create")
    public ResponseEntity<DefaultApiResponse<VirtualAccountDTO>> createVirtualAccount() {
        DefaultApiResponse<VirtualAccountDTO> response = virtualAccountService.createVirtualAccount();
        return ResponseEntity.status(HttpStatus.OK).body(response);
    }

    /**
     * Endpoint to get the details of a virtual account
     * @return ResponseEntity with the virtual account details
     */
    @Operation(summary = "Get Virtual Account for Employer")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Virtual bank account retrieved successfully"),
            @ApiResponse(responseCode = "400", description = "Unable to ger details of account")
    })
    @GetMapping("/details")
    public ResponseEntity<DefaultApiResponse<VirtualAccountDTO>> getVirtualAccountDetails() {
        DefaultApiResponse<VirtualAccountDTO> response = virtualAccountService.getVirtualAccount();
        return ResponseEntity.status(HttpStatus.OK).body(response);
    }

    /**
     * Endpoint to fetch transactions of a Virtual Bank Account (VBA)
     * @param startDate Optional start date for filtering transactions (format: YYYY-MM-DD)
     * @param endDate Optional end date for filtering transactions (format: YYYY-MM-DD)
     * @param page Optional page number for pagination (default: 1)
     * @param limit Optional limit on the number of transactions (default: 100)
     * @return ResponseEntity with the transaction details of the virtual account
     */
    @Operation(summary = "Get Transaction Directly Related to the Virtual Account from Kora Only and not bank transfer")
    // I just realised, I'll add for all transactions soon
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Transactions for Bank Account Retrieved Successfully"),
            @ApiResponse(responseCode = "400", description = "Unable to get Transactions for virtual account")
    })
    @GetMapping("/transactions")
    public ResponseEntity<DefaultApiResponse<VirtualAccountTransactionDTO>> getTransactionsOfVba(
            @RequestParam(required = false) String startDate,
            @RequestParam(required = false) String endDate,
            @RequestParam(required = false, defaultValue = "1") Integer page,
            @RequestParam(required = false, defaultValue = "100") Integer limit) {

        DefaultApiResponse<VirtualAccountTransactionDTO> response = virtualAccountService
                .getTransactionsOfVba(startDate, endDate, page, limit);
        return ResponseEntity.status(HttpStatus.OK).body(response);
    }

    /**
     * Endpoint for processing a bank transfer.
     *
     * @param amount The amount to be transferred.
     * @return A response entity containing the bank transfer details and the HTTP status code.
     */
    @Operation(summary = "Make Transfer to our Dashboard: Holds details for Transfer")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Bank transfer created successfully"),
            @ApiResponse(responseCode = "400", description = "Bank transfer initialization failed")
    })
    @PostMapping("/transfer") // POST request for processing bank transfer
    public ResponseEntity<DefaultApiResponse<BankTransferDetailsDTO>> processBankTransfer(
            @RequestParam BigDecimal amount) {
        DefaultApiResponse<BankTransferDetailsDTO> response = virtualAccountService.processBankTransfer(amount);
        return ResponseEntity.status(HttpStatus.OK).body(response);
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
    @GetMapping("/verify/{referenceNumber}") // GET request for verifying bank transfer
    public ResponseEntity<DefaultApiResponse<?>> verifyBankTransfer(
            @PathVariable String referenceNumber) {
        DefaultApiResponse<?> response = virtualAccountService.verifyBankTransfer(referenceNumber);
        return ResponseEntity.status(HttpStatus.OK).body(response);
    }


}
