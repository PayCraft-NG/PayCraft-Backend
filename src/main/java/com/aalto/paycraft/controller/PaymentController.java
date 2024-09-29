package com.aalto.paycraft.controller;

import com.aalto.paycraft.dto.DefaultApiResponse;
import com.aalto.paycraft.dto.PaymentDTO;
import com.aalto.paycraft.service.IPaymentService;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@Tag(
        name = "Payment Service Controller",
        description = "CRUD REST APIs to CREATE, READ, UPDATE Virtual Account details"
)
@RestController
@RequiredArgsConstructor
@RequestMapping(value = "api/v1/account/", produces = MediaType.APPLICATION_JSON_VALUE)
public class PaymentController {

    private final IPaymentService paymentService;

    @PostMapping("/pay")
    public ResponseEntity<DefaultApiResponse<PaymentDTO>> payEmployee(@RequestParam String employeeId){
        return ResponseEntity.status(HttpStatus.OK)
                .body(paymentService.payEmployee(UUID.fromString(employeeId)));
    }

//    @PostMapping("/pay")
//    public ResponseEntity<DefaultApiResponse<PaymentDTO>> payEmployeeBulk(@RequestParam String payrollId){
//        return ResponseEntity.status(HttpStatus.OK)
//                .body(paymentService.payEmployee(UUID.fromString(employeeId)));
//    }

}
