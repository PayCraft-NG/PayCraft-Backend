package com.aalto.paycraft.service;

import com.aalto.paycraft.dto.BankTransferResponseDTO;
import com.aalto.paycraft.dto.DefaultKoraResponse;
import com.aalto.paycraft.dto.VirtualAccountResponseDTO;
import com.aalto.paycraft.entity.Employer;
import com.aalto.paycraft.entity.VirtualAccount;
import org.springframework.http.ResponseEntity;

import java.math.BigDecimal;

public interface IWalletService {
    VirtualAccount createVirtualAccount(Employer employer);
    // New method to initiate a bank transfer
    DefaultKoraResponse<BankTransferResponseDTO> initiateBankTransfer(BigDecimal amount, String currency, Employer employer);
//    void cardFundingWithAPI();
//    void flexibleCardFundingAPI();
//    void payWithPayOutApi();
//    void payWithBulkPayOutApi();
//    void verifyPayoutTransaction();

//    https://webhook.site/d64965eb-84cb-4e4f-9d48-fd9b5360a029

}
