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

        List<Payroll> payrolls = payrollRepository.findAllWhereFrequencyIsNotNull();
        log.info("number of payrolls {}", payrolls.size());
        payrolls.forEach(payroll -> {
            if (shouldRunPayroll(payroll))
                processPayroll(payroll);
            else
                log.info("payroll {} doesn't match run criteria", payroll.getPayrollId());
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

        // Calculate the next run time and compare it with now. If it is true, then the run the job
        return LocalDate.now().isAfter(nextRunDate) || LocalDate.now().isEqual(nextRunDate);
    }

    private void processPayroll(Payroll payroll) {
        try{
            payroll.setPayPeriodStart(LocalDate.now());
            payroll.setLastRunDate(LocalDate.now());

            //todo: Implement the logic to process payroll and update totalSalary etc.

            payroll.setPaymentStatus(PaymentStatus.PAID);
            log.info("Successfully processed {}", payroll.getPayrollId())
            ;
        } catch (Exception e){
            payroll.setPaymentStatus(PaymentStatus.FAILED);
            log.info("An error occurred processing payroll {} {} {}", payroll.getPayrollId(), payroll.getCompany().getCompanyId(), payroll.getCompany().getCompanyName());
        } finally {
            payroll.setPayPeriodEnd(LocalDate.now());
            payrollRepository.save(payroll);
        }
    }
}
