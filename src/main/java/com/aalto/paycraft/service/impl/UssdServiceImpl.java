package com.aalto.paycraft.service.impl;

import com.aalto.paycraft.dto.PayrollDTO;
import com.aalto.paycraft.dto.UssdDTO;
import com.aalto.paycraft.entity.*;
import com.aalto.paycraft.repository.*;
import com.aalto.paycraft.service.IUssdService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class UssdServiceImpl implements IUssdService {
    private final EmployerRepository employerRepository;
    private final CompanyRepository companyRepository;
    private final PayrollRepository payrollRepository;
    private final VirtualAccountRepository virtualAccountRepository;
    private final CardRepository cardRepository;
    private final Integer[] line = {1};
    private final Integer[] amount = {0};

    @Override
    public String ussdCallback(UssdDTO ussdDTO) {
        String response = "";

        log.info("{}", ussdDTO.getPhoneNumber());
        // Remove '+' from phoneNumber
        if(ussdDTO.getPhoneNumber().contains("+"))  ussdDTO.setPhoneNumber(ussdDTO.getPhoneNumber().replace("+",""));

        // Verify PhoneNumber
        if(!verifyPhoneNumber(ussdDTO.getPhoneNumber())){
            response = "END Invalid phone number\n";
            return response;
        }

        if (ussdDTO.getText() == null || ussdDTO.getText().isEmpty()) {
            response = "CON Choose an option \n";
            response += "1. Fund Wallet with Card\n";
            response += "2. Account Details \n"; //Done
            response += "3. Run Payroll \n";
            response += "4. Payment History"; //Done
        } else if(ussdDTO.getText().equals("1")){
            response = "CON Enter Amount\n";
            // List of Cards
        } //todo: handle fund accounts follow-up
        else if (ussdDTO.getText().equals("2")){
            // Account Details
            response = getAccountDetails(ussdDTO.getPhoneNumber());
        }
        else if (ussdDTO.getText().equals("3")) {
            //Payrolls
            response = getPayrollsByCompanyNumber(ussdDTO.getPhoneNumber());
        }
        // TODO: handle payroll follow-up
        else if (ussdDTO.getText().equals("4")) {
            response = "END Request Successfully!\n";
            response += "Payment history will be sent to +" + ussdDTO.getPhoneNumber() + " via SMS";
        }
        return response;
    }

    private boolean verifyPhoneNumber(String phoneNumber){
        return employerRepository.existsByPhoneNumber(phoneNumber);
    }

    private String getAccountDetails(String phoneNumber){
        String response;

        Optional<Employer> employerOptional = employerRepository.findByPhoneNumber(phoneNumber);
        if(employerOptional.isPresent()) {
            Employer employer = employerOptional.get();
            Optional<VirtualAccount> accountOptional = virtualAccountRepository.findByEmployer_EmployerId(employer.getEmployerId());
            if (accountOptional.isPresent()){
                VirtualAccount account = accountOptional.get();
                response = "END Account Number: "+ account.getAccountNumber() + "\n";
                response += "Bank Name: " + account.getBankName() + "(" + account.getBankCode() + ")" + "\n";
                response += "Account Balance: "+ account.getBalance();
                return response;
            }
            else
                return "END No virtual account found";
        }
        else
            return "END Profile id does not exist";
    }

    private String getListOfCards(String phoneNumber){
        final String[] response = {"CON Enter first 6 digits of card to use"};

        Optional<Employer> employerOptional = employerRepository.findByPhoneNumber(phoneNumber);
        if(employerOptional.isPresent()) {
            Employer employer = employerOptional.get();
            Optional<VirtualAccount> accountOptional = virtualAccountRepository.findByEmployer_EmployerId(employer.getEmployerId());
            if (accountOptional.isPresent())
            {
                VirtualAccount account = accountOptional.get();
                List<Card> cards = cardRepository.findAllByAccount_AccountId(account.getAccountId());
                if(!cards.isEmpty()){
                    cards.forEach((card) -> {
                        response[0] +=  line[0] + " "+ card.getCardNumber() +"\n";
                        line[0]++;
                    });
                    return response[0];
                }
                else
                    return "END No cards found on the account";
            }
            else
                return "END No virtual account found";
        }
        else
            return "END Profile Id does not exist";
    }

    private String getPayrollsByCompanyNumber(String phoneNumber){
        final String[] response = {"CON List of runnable payrolls. Enter first 6 of payroll to run \n"};

        Optional<Company> companyOptional = companyRepository.findByCompanyPhoneNumber(phoneNumber);
        if(companyOptional.isPresent()) {
            Company company = companyOptional.get();
            List<Payroll> payrollList = payrollRepository.findAllByCompanyId(company.getCompanyId());
            if (!payrollList.isEmpty()){
                payrollList.forEach((payroll) -> {
                    response[0] += line[0] + " " + payroll.getPayrollId() + "\n";
                    line[0]++;
                });
                return response[0];
            }
            return "END No payroll found";
        }
        return "END No Company found with this phone number " + phoneNumber + "\n";
    }
}
