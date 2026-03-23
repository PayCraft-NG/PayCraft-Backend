package com.aalto.paycraft.controller;

import com.aalto.paycraft.dto.*;
import com.aalto.paycraft.service.IVirtualAccountService;
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

import java.math.BigDecimal;
import java.util.List;

@Tag(
        name = "Virtual account & funding",
        description = "Employer virtual account details, Kora transactions, bank transfer, card funding, and saved cards."
)
@SecurityRequirement(name = "bearerAuth")
@RestController
@RequiredArgsConstructor
@RequestMapping(value = "api/v1/account", produces = MediaType.APPLICATION_JSON_VALUE)
public class VirtualAccountController {

    private final IVirtualAccountService virtualAccountService;

    /**
     * Endpoint to get the details of a virtual account
     * @return ResponseEntity with the virtual account details
     */
    @Operation(summary = "Get virtual account details",
            description = "VBA details for the authenticated employer.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Virtual account retrieved"),
            @ApiResponse(responseCode = "400", description = "Unable to load account"),
            @ApiResponse(responseCode = "401", description = "Missing or invalid JWT")
    })
    @GetMapping("/details")
    public ResponseEntity<DefaultApiResponse<VirtualAccountDTO>> getVirtualAccountDetails() {
        DefaultApiResponse<VirtualAccountDTO> response = virtualAccountService.getVirtualAccount();
        return ResponseEntity.status(HttpStatus.OK).body(response);
    }

    /**
     * Endpoint to fetch payments of a Virtual Bank Account (VBA)
     * @param startDate Optional start date for filtering payments (format: YYYY-MM-DD)
     * @param endDate Optional end date for filtering payments (format: YYYY-MM-DD)
     * @param page Optional page number for pagination (default: 1)
     * @param limit Optional limit on the number of payments (default: 100)
     * @return ResponseEntity with the transaction details of the virtual account
     */
    @Operation(summary = "VBA transactions (Kora)",
            description = "Transactions from Kora for the virtual account, with optional date range and pagination.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Transactions retrieved"),
            @ApiResponse(responseCode = "400", description = "Invalid filters or upstream error"),
            @ApiResponse(responseCode = "401", description = "Missing or invalid JWT")
    })
    @GetMapping("/transactions")
    public ResponseEntity<DefaultApiResponse<VirtualAccountTransactionDTO>> getTransactionsOfVba(
            @Parameter(description = "Start date (YYYY-MM-DD)") @RequestParam(required = false) String startDate,
            @Parameter(description = "End date (YYYY-MM-DD)") @RequestParam(required = false) String endDate,
            @Parameter(description = "Page number (Kora pagination)") @RequestParam(required = false, defaultValue = "1") Integer page,
            @Parameter(description = "Page size limit") @RequestParam(required = false, defaultValue = "100") Integer limit) {

        DefaultApiResponse<VirtualAccountTransactionDTO> response = virtualAccountService
                .getTransactionsOfVba(startDate, endDate, page, limit);
        return ResponseEntity.status(HttpStatus.OK).body(response);
    }

    /**
     * Retrieves all payments for a virtual account with pagination support.
     * Allows fetching payments with a default page size of 10 if no size is provided.
     *
     * @param pageSize   The number of payments per page (defaults to 10).
     * @param pageNumber The page number to retrieve (defaults to 0, i.e., the first page).
     * @return A response containing payment data in a paginated format.
     */
    @Operation(summary = "List payments (paginated)",
            description = "All payments for the virtual account with application-level pagination.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Payments page"),
            @ApiResponse(responseCode = "400", description = "Invalid parameters"),
            @ApiResponse(responseCode = "401", description = "Missing or invalid JWT")
    })
    @GetMapping("/payments")
    public ResponseEntity<DefaultApiResponse<PaymentDataResponseDTO>> getAllPayments(
            @Parameter(description = "Items per page") @RequestParam(defaultValue = "10") int pageSize,
            @Parameter(description = "Zero-based page index") @RequestParam(defaultValue = "0") int pageNumber
    ) {

        // Call the service to fetch the paginated payment data
        DefaultApiResponse<PaymentDataResponseDTO> response = virtualAccountService.getAllPayments(pageSize, pageNumber);

        // Return the response wrapped in ResponseEntity
        return ResponseEntity.ok(response);
    }


    /**
     * Endpoint for processing a bank transfer.
     *
     * @param amount The amount to be transferred.
     * @return A response entity containing the bank transfer details and the HTTP status code.
     */
    @Operation(summary = "Initiate bank transfer to fund wallet",
            description = "Creates bank transfer instructions to fund the employer virtual balance.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Transfer details returned"),
            @ApiResponse(responseCode = "400", description = "Transfer could not be started"),
            @ApiResponse(responseCode = "401", description = "Missing or invalid JWT")
    })
    @PostMapping("/transfer")
    public ResponseEntity<DefaultApiResponse<BankTransferDetailsDTO>> processBankTransfer(
            @Parameter(description = "Amount to transfer", required = true) @RequestParam BigDecimal amount) {
        DefaultApiResponse<BankTransferDetailsDTO> response = virtualAccountService.processBankTransfer(amount);
        return ResponseEntity.status(HttpStatus.OK).body(response);
    }

    /**
     * Endpoint for processing a bank transfer.
     *
     * @param requestDTO The body holding the card details
     * @return A response entity containing the payment data details and the HTTP status code.
     */
    @Operation(summary = "Fund via card",
            description = "Initialize card payment to fund the virtual account.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Card funding initiated or completed"),
            @ApiResponse(responseCode = "400", description = "Invalid card payload or provider error"),
            @ApiResponse(responseCode = "401", description = "Missing or invalid JWT")
    })
    @PostMapping("/fund-card")
    public ResponseEntity<DefaultApiResponse<?>> fundByCard(
            @RequestBody CardFundingRequestDTO requestDTO) {
        DefaultApiResponse<?> response = virtualAccountService.processCardFunding(requestDTO);
        return ResponseEntity.status(HttpStatus.OK).body(response);
    }

    /**
     * Endpoint for verifying a bank transfer using a reference number.
     *
     * @param referenceNumber The reference number of the bank transfer.
     * @return A response entity indicating the status of the bank transfer verification.
     */
    @Operation(summary = "Verify bank transfer",
            description = "Poll transfer status by reference number.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Verification result"),
            @ApiResponse(responseCode = "400", description = "Verification failed"),
            @ApiResponse(responseCode = "401", description = "Missing or invalid JWT")
    })
    @GetMapping("/verify/{referenceNumber}")
    public ResponseEntity<DefaultApiResponse<?>> verifyBankTransfer(
            @Parameter(description = "Transfer reference", required = true) @PathVariable String referenceNumber) {
        DefaultApiResponse<?> response = virtualAccountService.verifyPayment(referenceNumber);
        return ResponseEntity.status(HttpStatus.OK).body(response);
    }


    // GET ALL Cards belonging to the virtual account
    @GetMapping("/card/all")
    @Operation(summary = "List saved cards",
            description = "Cards on file for the employer's virtual account.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Cards retrieved"),
            @ApiResponse(responseCode = "401", description = "Missing or invalid JWT"),
            @ApiResponse(responseCode = "404", description = "Virtual account not found")
    })
    public ResponseEntity<DefaultApiResponse<List<CardRequestDTO>>> getAllCards(){
        return ResponseEntity.status(HttpStatus.OK).body(virtualAccountService.getCardsForEmployer());
    }


    // Add a new card to the virtual account
    @PostMapping("/card/add")
    @Operation(summary = "Add saved card",
            description = "Tokenize or store a card for the virtual account.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Card saved"),
            @ApiResponse(responseCode = "400", description = "Invalid card data"),
            @ApiResponse(responseCode = "401", description = "Missing or invalid JWT"),
            @ApiResponse(responseCode = "404", description = "Virtual account not found")
    })
    public ResponseEntity<DefaultApiResponse<CardRequestDTO>> addCard(@RequestBody CardRequestDTO requestDTO){
        return ResponseEntity.status(HttpStatus.OK).body(virtualAccountService.saveCard(requestDTO));
    }

    // Delete a card belonging to the virtual account
    @DeleteMapping("/card/delete")
    @Operation(summary = "Remove saved card",
            description = "Delete a stored card by id.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Card removed"),
            @ApiResponse(responseCode = "401", description = "Missing or invalid JWT"),
            @ApiResponse(responseCode = "404", description = "Card or virtual account not found")
    })
    public ResponseEntity<DefaultApiResponse<?>> deleteCard(
            @Parameter(description = "Saved card id", required = true) @RequestParam Long cardId) {
        return ResponseEntity.status(HttpStatus.OK).body(virtualAccountService.deleteCard(cardId));
    }

}
