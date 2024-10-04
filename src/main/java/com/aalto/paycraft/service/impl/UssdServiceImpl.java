package com.aalto.paycraft.service.impl;

import com.aalto.paycraft.dto.CardFundingRequestDTO;
import com.aalto.paycraft.dto.UssdDTO;
import com.aalto.paycraft.entity.*;
import com.aalto.paycraft.repository.*;
import com.aalto.paycraft.service.IPayrollService;
import com.aalto.paycraft.service.IUssdService;
import com.aalto.paycraft.service.IVirtualAccountService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
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
    private final IVirtualAccountService virtualAccountService;
    private final IPayrollService payrollService;
    private final Integer[] line = {1};
    private final Integer[] amount = {0};

    @Override
    public String ussdCallback(UssdDTO ussdDTO) {
        String response = "";

        ussdDTO.setText(ussdDTO.getText().trim());
        log.info("{}", ussdDTO.getPhoneNumber());
        log.info("{}", ussdDTO.getText());

        // Remove '+' from phoneNumber
        if(ussdDTO.getPhoneNumber().contains("+"))  ussdDTO.setPhoneNumber(ussdDTO.getPhoneNumber().replace("+",""));

        // Verify PhoneNumber
        if(!verifyPhoneNumber(ussdDTO.getPhoneNumber())){
            response = "END Invalid phone number\n";
            return response;
        }

        // Store amount
        if (ussdDTO.getText().matches("^1\\*\\w+")) {
            String extractedPart = ussdDTO.getText().substring(2); // Extract the part after "1*"

            try {
                amount[0] = Integer.parseInt(extractedPart); // Parse the extracted part as an integer
                log.info("===== amount: {} =====", amount[0]);
                return getListOfCards(ussdDTO.getPhoneNumber()); // Return the list of cards
            } catch (Exception exception) {
                log.error("Invalid input: could not parse amount from {}", extractedPart);
                return "END Invalid Input";
            }
        }

        if (ussdDTO.getText().matches("^1\\*.*\\*.*")) {
            String cardPart = ussdDTO.getText().substring(ussdDTO.getText().lastIndexOf('*') + 1); // Extract the part after the second "*"
            log.info("Extracted card part: {}", cardPart);
            log.info("amount: {}", amount[0]);
            return findAndFundWithCard(cardPart, ussdDTO.getPhoneNumber()); // Call findCard with extracted part
        }


        if (ussdDTO.getText().matches("^3\\*\\s*\\w+")) {
            String extractedPart = ussdDTO.getText().substring(2); // Extract the part after "1*"
            log.info("Extracted payroll ID: {}", extractedPart);
            return findAndRunManualPayroll(extractedPart);
        }


        if (ussdDTO.getText().isEmpty()) {
            response = "CON Choose an option \n";
            response += "1. Fund Wallet with Card\n";
            response += "2. Account Details \n"; //Done
            response += "3. Run Payroll \n";
            response += "4. Payment History"; //Done
        } else if(ussdDTO.getText().equals("1")) {
            response = "CON Enter Amount\n";
            // List of Cards
        }
        else if (ussdDTO.getText().equals("2")){
            // Account Details
            response = getAccountDetails(ussdDTO.getPhoneNumber());
        }
        else if (ussdDTO.getText().equals("3")) {
            //Payrolls
            response = getPayrollsByCompanyNumber(ussdDTO.getPhoneNumber());
        }
        else if (ussdDTO.getText().equals("4")) {
            response = "END Request Successfully!\n";
            response += "Payment history will be sent to +" + ussdDTO.getPhoneNumber() + " via SMS\n";
        }
        return response;
    }

    private String findAndFundWithCard(String extracted, String phoneNumber) {
        if (extracted.length() != 6) return "END Invalid Input (Card length too short)\n";

        Optional<Employer> employerOptional = employerRepository.findByPhoneNumber(phoneNumber);
        if (employerOptional.isPresent()) {
            Employer employer = employerOptional.get();
            Optional<VirtualAccount> accountOptional = virtualAccountRepository.findByEmployer_EmployerId(employer.getEmployerId());
            if (accountOptional.isPresent()) {
                VirtualAccount account = accountOptional.get();
                List<Card> cardsList = cardRepository.findAllByAccount_AccountId(account.getAccountId());

                if (cardsList.isEmpty()) return "END No Cards found on this account\n";

                CardFundingRequestDTO cardFundingRequestDTO = null;
                for (Card card : cardsList) {
                    if (card.getCardNumber().startsWith(extracted)) {
                        cardFundingRequestDTO = getCardFundingRequestDTO(card);
                        break;
                    }
                }

                if (cardFundingRequestDTO != null) {
                    try {
                        virtualAccountService.processCardFunding(cardFundingRequestDTO);
                        return "END Successfully funded account\nAn SMS will be sent shortly to confirm\n";
                    } catch (Exception e) {
                        return "END Error occurred when funding account with " + cardFundingRequestDTO.getCardNumber() + "\n";
                    }
                } else {
                    return "END You must have entered the wrong first 6 digits\n";
                }
            } else {
                return "END No virtual account found\n";
            }
        } else {
            return "END Profile does not exist\n";
        }
    }


    private CardFundingRequestDTO getCardFundingRequestDTO(Card card) {
        CardFundingRequestDTO cardFundingRequestDTO = new CardFundingRequestDTO();
        cardFundingRequestDTO.setCardNumber(card.getCardNumber());
        cardFundingRequestDTO.setCardPin(card.getCardPin());
        cardFundingRequestDTO.setCvv(card.getCvv());
        cardFundingRequestDTO.setAmount(BigDecimal.valueOf(amount[0]));
        cardFundingRequestDTO.setExpiryYear(card.getExpiryYear());
        cardFundingRequestDTO.setExpiryMonth(card.getExpiryMonth());
        return cardFundingRequestDTO;
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
                return "END No virtual account found\n";
        }
        else
            return "END Profile does not exist\n";
    }

    private String getListOfCards(String phoneNumber){
        final String[] response = {"CON Enter first 6 digits of card to fund account\n"};

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
                        response[0] +=  line[0] + ". "+ card.getCardNumber() +"\n";
                        line[0]++;
                    });
                    line[0] = 1;
                    return response[0];
                }
                else
                    return "END No cards found on the account\n";
            }
            else
                return "END No virtual account found\n";
        }
        else
            return "END Profile does not exist\n";
    }

    private String findAndRunManualPayroll(String payrollName){
        Optional<Payroll> payrollOptional = payrollRepository.findOneWhereAutomaticIsFalseByPayrollName(payrollName);
        if(payrollOptional.isPresent()){
            Payroll payroll = payrollOptional.get();
            payrollService.runPayroll(payroll.getPayrollId());
            return "END Payroll manual ran successfully\n" + "An SMS will be sent shortly to confirm";
        }
        return "END Invalid payroll name";
    }

    private String getPayrollsByCompanyNumber(String phoneNumber){
        final String[] response = {"CON List of runnable payrolls. Enter payroll name in full to run\n"};

        Optional<Company> companyOptional = companyRepository.findByCompanyPhoneNumber(phoneNumber);
        if(companyOptional.isPresent()) {
            Company company = companyOptional.get();
            List<Payroll> payrollList = payrollRepository.findAllWhereAutomaticIsFalseByCompanyId(company.getCompanyId());
            if (!payrollList.isEmpty()){
                payrollList.forEach((payroll) -> {
                    response[0] += line[0] + ". " + payroll.getPayrollName() + "\n";
                    line[0]++;
                });
                line[0] = 1;
                return response[0];
            }
            return "END No manual payroll found\n";
        }
        return "END No Company found with this phone number " + phoneNumber + "\n";
    }
}
