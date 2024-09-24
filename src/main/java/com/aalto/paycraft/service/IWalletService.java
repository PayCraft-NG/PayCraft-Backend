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
}
