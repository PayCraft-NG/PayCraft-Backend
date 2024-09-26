package com.aalto.paycraft.controller;

import com.aalto.paycraft.dto.BankTransferResponseDTO;
import com.aalto.paycraft.dto.DefaultKoraResponse;
import com.aalto.paycraft.entity.Employer;
import com.aalto.paycraft.repository.EmployerRepository;
import com.aalto.paycraft.service.IWalletService;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.Optional;
import java.util.UUID;

@RestController
@RequiredArgsConstructor
@RequestMapping(value = "/api/v1/wallet", produces = MediaType.APPLICATION_JSON_VALUE)
public class WalletController {

    private final IWalletService walletService;
    private final EmployerRepository employerRepository;

    private static final Logger logger = LoggerFactory.getLogger(KoraPayWebhookController.class);

    @PostMapping("/fund")
    public DefaultKoraResponse<BankTransferResponseDTO> handleKoraPayWebHook(@RequestParam String employerId )  {
        Employer emp = new Employer();
        Optional<Employer> employer = employerRepository.findByEmployerId(UUID.fromString(employerId));
        if (employer.isPresent()) {
            emp = employer.get();
        }

        try {
            return walletService.initiateBankTransfer(BigDecimal.valueOf(2000L) ,"NGN" , emp);
        } catch (RuntimeException e) {
            logger.error("Error processing request", e);
        }
        return null;
    }

}
