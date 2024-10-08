package com.aalto.paycraft.controller;

import com.aalto.paycraft.dto.*;
import com.aalto.paycraft.entity.Card;
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
import java.util.List;

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
     * Endpoint to fetch payments of a Virtual Bank Account (VBA)
     * @param startDate Optional start date for filtering payments (format: YYYY-MM-DD)
     * @param endDate Optional end date for filtering payments (format: YYYY-MM-DD)
     * @param page Optional page number for pagination (default: 1)
     * @param limit Optional limit on the number of payments (default: 100)
     * @return ResponseEntity with the transaction details of the virtual account
     */
    @Operation(summary = "Get Payment Directly Related to the Virtual Account from Kora Only and not bank transfer")
    // I just realised, I'll add for all payments soon
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Payment for Bank Account Retrieved Successfully"),
            @ApiResponse(responseCode = "400", description = "Unable to get Payment for virtual account")
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
     * Retrieves all payments for a virtual account with pagination support.
     * Allows fetching payments with a default page size of 10 if no size is provided.
     *
     * @param pageSize   The number of payments per page (defaults to 10).
     * @param pageNumber The page number to retrieve (defaults to 0, i.e., the first page).
     * @return A response containing payment data in a paginated format.
     */
    @Operation(summary = "Retrieve all payments", description = "Fetch all payments for a virtual account with pagination. Default page size is 10.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successfully fetched payments"),
            @ApiResponse(responseCode = "400", description = "Invalid request parameters"),
    })
    @GetMapping("/payments")
    public ResponseEntity<DefaultApiResponse<PaymentDataResponseDTO>> getAllPayments(
            @RequestParam(defaultValue = "10") int pageSize,  // Default pageSize is 10
            @RequestParam(defaultValue = "0") int pageNumber  // Default pageNumber is 0 (first page)
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
     * Endpoint for processing a bank transfer.
     *
     * @param requestDTO The body holding the card details
     * @return A response entity containing the payment data details and the HTTP status code.
     */
    @Operation(summary = "Make Transfer to our Dashboard: Holds details for Transfer")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Bank transfer created successfully"),
            @ApiResponse(responseCode = "400", description = "Bank transfer initialization failed")
    })
    @PostMapping("/fund-card") // POST request for processing card funding
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
    @Operation(summary = "Verifies the Bank Transfer Made")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Bank transfer successful"),
            @ApiResponse(responseCode = "400", description = "Bank transfer failed")
    })
    @GetMapping("/verify/{referenceNumber}") // GET request for verifying bank transfer
    public ResponseEntity<DefaultApiResponse<?>> verifyBankTransfer(
            @PathVariable String referenceNumber) {
        DefaultApiResponse<?> response = virtualAccountService.verifyPayment(referenceNumber);
        return ResponseEntity.status(HttpStatus.OK).body(response);
    }


    // GET ALL Cards belonging to the virtual account
    @GetMapping("/card/all")
    @Operation(summary = "Get all cards for a virtual account")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "All cards retrieved"),
            @ApiResponse(responseCode = "404", description = "Virtual account not found")
    })
    public ResponseEntity<DefaultApiResponse<List<CardRequestDTO>>> getAllCards(){
        return ResponseEntity.status(HttpStatus.OK).body(virtualAccountService.getCardsForEmployer());
    }


    // Add a new card to the virtual account
    @PostMapping("/card/add")
    @Operation(summary = "Add a new card to the virtual account")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Card successfully added"),
            @ApiResponse(responseCode = "404", description = "Virtual account not found")
    })
    public ResponseEntity<DefaultApiResponse<CardRequestDTO>> addCard(@RequestBody CardRequestDTO requestDTO){
        return ResponseEntity.status(HttpStatus.OK).body(virtualAccountService.saveCard(requestDTO));
    }

    // Delete a card belonging to the virtual account
    @DeleteMapping("/card/delete")
    @Operation(summary = "Delete card belonging to a virtual account")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Card successfully removed"),
            @ApiResponse(responseCode = "404", description = "Virtual account not found")
    })
    public ResponseEntity<DefaultApiResponse<?>> deleteCard(@RequestParam Long cardId){
        return ResponseEntity.status(HttpStatus.OK).body(virtualAccountService.deleteCard(cardId));
    }

}
