package com.aalto.paycraft.service;

import com.aalto.paycraft.dto.*;
import com.aalto.paycraft.entity.Employer;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

public interface IVirtualAccountService {

    UUID createVirtualAccount(Employer employer);
    DefaultApiResponse<VirtualAccountDTO> getVirtualAccount();
    DefaultApiResponse<VirtualAccountTransactionDTO> getTransactionsOfVba(String startDate, String endDate, Integer page, Integer limit);
    DefaultApiResponse<PaymentDataResponseDTO> getAllPayments(int pageSize, int pageNumber);

    // Make Transfer Related Operations
    DefaultApiResponse<BankTransferDetailsDTO> processBankTransfer(BigDecimal amount);
    DefaultApiResponse<PaymentDTO> verifyPayment(String referenceNumber); // This would work for both to fixedVirtualAccount or BankTransfer

    // Card Related Items
    DefaultApiResponse<?> processCardFunding(CardFundingRequestDTO requestBody);

    DefaultApiResponse<List<CardRequestDTO>> getCardsForEmployer();

    DefaultApiResponse<CardRequestDTO> saveCard(CardRequestDTO requestBody);

    DefaultApiResponse<?> deleteCard(Long cardId);
}
