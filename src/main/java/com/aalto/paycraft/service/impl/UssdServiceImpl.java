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
            response = "CON Choose an option \n";
            response += "1. Fund Wallet \n";
            response += "2. Account Details \n";
            response += "3. Run Payroll \n";
            response += "4. Payroll History";
        } else if(ussdDTO.getText().equals("1")){
            response = "CON Enter Amount to Fund \n";
            response += "1. ****8624\n";
            response += "2. ****9493\n";
        } else if(ussdDTO.getText().equals("1*1")){
            response = "END Request for *****8624 Sent!\n";
            response += "You will receive an SMS shortly";
        }else if(ussdDTO.getText().equals("1*2")){
            response = "END Request for *****9493 Sent!\n";
            response += "You will receive an SMS shortly";
        }
        else if (ussdDTO.getText().equals("2")){
            response = "END Account Number: 1234567890\n";
            response += "Account Balance: NGN2,000,000";
        }
        else if (ussdDTO.getText().equals("3")) {
            response += "CON List of runnable payrolls.\n";
            response += "Enter the payroll to run: \n";
            response += "1. Contractor payroll (KLK)\n";
            response += "2. Part-time payroll (POI)\n";
            response += "3. Full-time payroll (UYT)\n";
        } else if(ussdDTO.getText().equals("3*1")){
            response = "END Request to run (KLK) was successfully";
        } else if(ussdDTO.getText().equals("3*2")){
            response = "END Request to run (POI) was successfully";
        } else if(ussdDTO.getText().equals("3*3")){
            response = "END Request to run (UYT) was successfully";
        }
        else if (ussdDTO.getText().equals("4")) {
            response = "END Request Successfully!\n";
            response += "An SMS will be sent to +" + ussdDTO.getPhoneNumber() + " shortly";
        }
        return response;

    }
}