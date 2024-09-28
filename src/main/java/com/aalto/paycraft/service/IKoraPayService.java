package com.aalto.paycraft.service;

import com.aalto.paycraft.dto.*;
import com.aalto.paycraft.entity.Employer;
import com.aalto.paycraft.entity.VirtualAccount;

import java.math.BigDecimal;

public interface IKoraPayService {
    // Virtual Bank Account
    DefaultKoraResponse<VirtualAccountResponseDTO> createVirtualAccount(Employer employer);
    DefaultKoraResponse<VBATransactionDTO> getTransactionOfVBA(String accountNumber, Employer employer,
                                                               String startDate, String endDate, Integer page, Integer limit);

    // Bank Transfer
    DefaultKoraResponse<BankTransferResponseDTO> initiateBankTransfer(BigDecimal amount, Employer employer);
}
