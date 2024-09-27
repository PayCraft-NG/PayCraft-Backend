package com.aalto.paycraft.controller;

import com.aalto.paycraft.dto.BankTransferDetailsDTO;
import com.aalto.paycraft.dto.DefaultApiResponse;
import com.aalto.paycraft.dto.VirtualAccountDTO;
import com.aalto.paycraft.dto.VirtualAccountTransactionDTO;
import com.aalto.paycraft.service.IVirtualAccountService;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;

@RestController
@RequiredArgsConstructor
@RequestMapping(value = "api/v1/account", produces = MediaType.APPLICATION_JSON_VALUE)
@Tag(name = "Virtual Account Management", description = "Endpoints for managing wallet")
public class VirtualAccountController {

    private final IVirtualAccountService virtualAccountService;

    /**
     * Endpoint to create a virtual account
     * @return ResponseEntity with the created virtual account details
     */
    @PostMapping("/create")
    public ResponseEntity<DefaultApiResponse<VirtualAccountDTO>> createVirtualAccount() {
        DefaultApiResponse<VirtualAccountDTO> response = virtualAccountService.createVirtualAccount();
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
    @GetMapping("/transactions")
    public ResponseEntity<DefaultApiResponse<VirtualAccountTransactionDTO>> getTransactionsOfVba(
            @RequestParam(required = false) String startDate,
            @RequestParam(required = false) String endDate,
            @RequestParam(required = false, defaultValue = "1") Integer page,
            @RequestParam(required = false, defaultValue = "100") Integer limit) {

        DefaultApiResponse<VirtualAccountTransactionDTO> response = virtualAccountService.getTransactionsOfVba(startDate, endDate, page, limit);
        return ResponseEntity.status(HttpStatus.OK).body(response);
    }

    /**
     * Endpoint for processing a bank transfer.
     *
     * @param amount The amount to be transferred.
     * @return A response entity containing the bank transfer details and the HTTP status code.
     */
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
    @GetMapping("/verify/{referenceNumber}") // GET request for verifying bank transfer
    public ResponseEntity<DefaultApiResponse<?>> verifyBankTransfer(
            @PathVariable String referenceNumber) {
        DefaultApiResponse<?> response = virtualAccountService.verifyBankTransfer(referenceNumber);
        return ResponseEntity.status(HttpStatus.OK).body(response);
    }


}
