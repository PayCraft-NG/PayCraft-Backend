package com.aalto.paycraft.job;

import com.aalto.paycraft.dto.enums.PaymentStatus;
import com.aalto.paycraft.entity.Payroll;
import com.aalto.paycraft.repository.PayrollRepository;
import com.aalto.paycraft.dto.enums.PayrollFrequency;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import java.time.LocalDate;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class PayrollJob {
    private final PayrollRepository payrollRepository;

    @Scheduled(fixedRateString = "${payroll.job.fixedRate}")
    public void checkAndRunPayrolls() {
        log.info("===== Running Payroll Job =====");

        List<Payroll> payrolls = payrollRepository.findAll();
        log.info("number of payrolls {}", payrolls.size());
        payrolls.forEach(payroll -> {
            if (payroll.getAutomatic() && shouldRunPayroll(payroll)) {
                processPayroll(payroll); // Custom method to run the payroll
            }
        });
    }

    private boolean shouldRunPayroll(Payroll payroll) {
        LocalDate lastRunDate = payroll.getLastRunDate();
        PayrollFrequency frequency = payroll.getFrequency();

        if (lastRunDate == null || frequency == null) {
            log.info("===== payroll job failed =====");
            return false;
        }

        LocalDate nextRunDate = switch (frequency) {
            case DAILY -> lastRunDate.plusDays(1);
            case WEEKLY -> lastRunDate.plusWeeks(1);
            case MONTHLY -> lastRunDate.plusMonths(1);
            case YEARLY -> lastRunDate.plusYears(1);
        };

        // Calculate the next run time and compare it with now. If it is true, then the job is run to be run.
        return LocalDate.now().isAfter(nextRunDate) || LocalDate.now().isEqual(nextRunDate);
    }

    private void processPayroll(Payroll payroll) {
        try{
            payroll.setPayPeriodStart(LocalDate.now());
            payroll.setLastRunDate(LocalDate.now()); // Update the last run date

            //todo: Implement the logic to process payroll, update status, etc.
            payroll.setPaymentStatus(PaymentStatus.PAID);
        } catch (Exception e){
            payroll.setPaymentStatus(PaymentStatus.FAILED);
            log.info("An error occurred processing payroll {} {} {}", payroll.getPayrollId(), payroll.getCompany().getCompanyId(), payroll.getCompany().getCompanyName());
        } finally {
            payroll.setPayPeriodEnd(LocalDate.now());
            payrollRepository.save(payroll);
        }
    }
}
