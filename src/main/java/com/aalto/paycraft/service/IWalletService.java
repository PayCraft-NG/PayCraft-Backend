package com.aalto.paycraft.service;

import com.aalto.paycraft.dto.*;
import com.aalto.paycraft.entity.Employer;
import com.aalto.paycraft.entity.VirtualAccount;
import org.springframework.http.ResponseEntity;

import java.math.BigDecimal;

public interface IWalletService {
    VirtualAccount createVirtualAccount(Employer employer);
    DefaultKoraResponse<BankTransferResponseDTO> initiateBankTransfer(BigDecimal amount, String currency, Employer employer);
    DefaultApiResponse<?> chargeCard(CardFundingRequestDTO payload, Employer employer) throws Exception;

//    void payWithPayOutApi();
//    void payWithBulkPayOutApi();
//    void verifyPayoutTransaction();
}
