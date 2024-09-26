package com.aalto.paycraft.controller;

import com.aalto.paycraft.dto.BankTransferResponseDTO;
import com.aalto.paycraft.dto.CardFundingRequestDTO;
import com.aalto.paycraft.dto.DefaultApiResponse;
import com.aalto.paycraft.dto.DefaultKoraResponse;
import com.aalto.paycraft.entity.Employer;
import com.aalto.paycraft.entity.VirtualAccount;
import com.aalto.paycraft.repository.EmployerRepository;
import com.aalto.paycraft.service.IWalletService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.Optional;
import java.util.UUID;

@RestController @Slf4j
@RequiredArgsConstructor
@RequestMapping(value = "/api/v1/wallet", produces = MediaType.APPLICATION_JSON_VALUE)
public class WalletController {

    private final IWalletService walletService;
    private final EmployerRepository employerRepository;
    private final String employerId = "6ff8ac68-b50e-4991-ba01-53e606dc7ab6";

    private Employer EMPLOYER(String employerId){
        Employer emp = new Employer();
        Optional<Employer> employer = employerRepository.findByEmployerId(UUID.fromString(employerId));
        if (employer.isPresent()) emp = employer.get();
        return emp;
    }

    @PostMapping("/create/{employerId}")
    public ResponseEntity<VirtualAccount> createVirtualAccount(@PathVariable String employerId){
        return ResponseEntity.status(200).body(walletService.createVirtualAccount(EMPLOYER(employerId)));
    }

//    @GetMapping("/details")
//    public ResponseEntity<VirtualAccount> getVirtualAccount(){
//        return ResponseEntity.status(200).body(walletService.getVirtualAccount(EMPLOYER()));
//    }

    @PostMapping("/card/fund")
    public ResponseEntity<DefaultApiResponse<?>> chargeCard(@RequestBody CardFundingRequestDTO requestDTO) throws Exception {
        return ResponseEntity.status(200).body(walletService.chargeCard(requestDTO, EMPLOYER(employerId)));
    }

    @PostMapping("/fund")
    public DefaultKoraResponse<BankTransferResponseDTO> makeTransferToOurBalance()  {
        try {
            return walletService.initiateBankTransfer(BigDecimal.valueOf(2000L) ,"NGN" , EMPLOYER(employerId));
        } catch (RuntimeException e) {
            log.error("Error processing request", e);
            return null;
        }
    }

}
