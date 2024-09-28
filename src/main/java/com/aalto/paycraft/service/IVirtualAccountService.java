package com.aalto.paycraft.service;

import com.aalto.paycraft.dto.BankTransferDetailsDTO;
import com.aalto.paycraft.dto.DefaultApiResponse;
import com.aalto.paycraft.dto.VirtualAccountDTO;
import com.aalto.paycraft.dto.VirtualAccountTransactionDTO;

import java.math.BigDecimal;

public interface IVirtualAccountService {
    // Virtual Account Details Related
    DefaultApiResponse<VirtualAccountDTO> createVirtualAccount();
    DefaultApiResponse<VirtualAccountDTO> getVirtualAccount();
    DefaultApiResponse<VirtualAccountTransactionDTO> getTransactionsOfVba(String startDate, String endDate, Integer page, Integer limit);

    // Make Transfer Related Operations
    DefaultApiResponse<BankTransferDetailsDTO> processBankTransfer(BigDecimal amount);
    DefaultApiResponse<?> verifyBankTransfer(String referenceNumber);
}
