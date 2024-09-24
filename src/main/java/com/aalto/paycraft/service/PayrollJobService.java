package com.aalto.paycraft.service;

import com.aalto.paycraft.dto.enums.PaymentStatus;
import com.aalto.paycraft.entity.Payroll;
import com.aalto.paycraft.repository.PayrollRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.scheduling.TaskScheduler;
import org.springframework.scheduling.support.CronTrigger;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ScheduledFuture;

@Slf4j
@Service
@RequiredArgsConstructor
public class PayrollJobService implements CommandLineRunner {
    private final TaskScheduler taskScheduler;
    private final PayrollRepository payrollRepository;
    private final Map<UUID, ScheduledFuture<?>> scheduledFutureMap = new HashMap<>();

    @Override
    public void run(String... args) throws Exception {
        initializePayrollJobs();
    }

    public void initializePayrollJobs(){
        // re-collect payrolls in db during build time
        List<Payroll> payrollList = payrollRepository.findAddWhereAutomaticIsTrue();
        payrollList.forEach(
                this::schedulePayroll
        );
        log.info("payroll list size: {}", payrollList.size());
    }

    public void schedulePayroll(Payroll payroll){
        // cancel payroll job if it already exists
        // helps handle update and create new payroll
        cancelScheduledPayroll(payroll.getPayrollId());

        if(payroll.getCronExpression() != null && !payroll.getCronExpression().isEmpty()){
            log.info("===== schedule payroll created =====");
            CronTrigger cronTrigger = new CronTrigger(payroll.getCronExpression());
            ScheduledFuture<?> scheduledTask = taskScheduler.schedule(()-> processPayroll(payroll), cronTrigger);
            scheduledFutureMap.put(payroll.getPayrollId(), scheduledTask);
        }
    }

    public void cancelScheduledPayroll(UUID payrollId){
        ScheduledFuture<?> scheduledTask = scheduledFutureMap.get(payrollId);
        if (scheduledTask != null){
            scheduledTask.cancel(true);
            scheduledFutureMap.remove(payrollId);
            log.info("===== schedule payroll cancelled =====");
        }
    }

    public void processPayroll(Payroll payroll){
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
