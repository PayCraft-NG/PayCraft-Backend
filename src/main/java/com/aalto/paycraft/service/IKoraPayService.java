package com.aalto.paycraft.service;

import com.aalto.paycraft.dto.*;
import com.aalto.paycraft.entity.Employer;
import com.aalto.paycraft.entity.VirtualAccount;

import java.math.BigDecimal;
import java.util.List;

public interface IKoraPayService {
    // Virtual Bank Account
    DefaultKoraResponse<VirtualAccountResponseDTO> createVirtualAccount(Employer employer);
    DefaultKoraResponse<VBATransactionDTO> getTransactionOfVBA(String accountNumber, Employer employer,
                                                               String startDate, String endDate, Integer page, Integer limit);

    // Bank Transfer
    DefaultKoraResponse<BankTransferResponseDTO> initiateBankTransfer(BigDecimal amount, Employer employer);

    // Payout
    DefaultKoraResponse<List<BankTypeDTO>> listBanks() throws Exception;
    DefaultKoraResponse<BankAccountDTO> resolveBankAccount(String bankCode, String accountNumber) throws Exception;
    DefaultKoraResponse<PayoutResponseDTO> requestPayout(String bankCode, String accountNumber, BigDecimal amount, Employer employer) throws Exception;
    DefaultKoraResponse<BulkPayoutResponseDTO> requestBulkPayout(List<PayoutData> payrollList, Employer employer) throws Exception;
}
