package com.aalto.paycraft.service.impl;

import com.aalto.paycraft.dto.UssdDTO;
import com.aalto.paycraft.service.IUssdService;
import org.springframework.stereotype.Service;

@Service
public class UssdServiceImpl implements IUssdService {
    @Override
    public String ussdCallback(UssdDTO ussdDTO) {
        String response = "";

        if (ussdDTO.getText() == null || ussdDTO.getText().isEmpty()) {
            response = "CON What would you want to check \n";
            response += "1. Account Number \n";
            response += "2. Phone Number";
        } else if (ussdDTO.getText().equals("1")) {
            response = "END Account Number: " + "8077938947\n";
            response += "Account Balance: " + "NGN2,066,961.00";
        } else if (ussdDTO.getText().equals("2")) {
            response = "END Phone Number: " + ussdDTO.getPhoneNumber();
        }
        return response;
    }
}