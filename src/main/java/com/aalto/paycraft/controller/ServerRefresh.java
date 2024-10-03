package com.aalto.paycraft.controller;

import com.aalto.paycraft.constants.PayCraftConstant;
import com.aalto.paycraft.dto.DefaultApiResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/")
public class ServerRefresh {
    @GetMapping("/")
    public ResponseEntity<DefaultApiResponse<String>> serverRefresh(){
        DefaultApiResponse<String> response = new DefaultApiResponse<>();
        response.setStatusCode(PayCraftConstant.REQUEST_SUCCESS);
        response.setData("Refreshed!");
        response.setStatusMessage("Successfully Refreshed!");
        return ResponseEntity.status(HttpStatus.OK).body(response);
    }
}
