package com.aalto.paycraft.service;

import com.aalto.paycraft.dto.BulkPayoutResponseDTO;
import com.aalto.paycraft.dto.DefaultApiResponse;
import com.aalto.paycraft.dto.enums.PaymentStatus;
import com.aalto.paycraft.entity.Payroll;
import com.aalto.paycraft.repository.PayrollRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.scheduling.TaskScheduler;
import org.springframework.scheduling.support.CronTrigger;
import org.springframework.stereotype.Service;
import org.thymeleaf.context.Context;

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
    private final IPaymentService paymentService;
    private final PayrollRepository payrollRepository;
    private final Map<UUID, ScheduledFuture<?>> scheduledFutureMap = new HashMap<>();
    private final IEmailService emailService;

    @Value("${spring.mail.enable}")
    private Boolean enableEmail;

    @Value("${frontend.url}")
    private String frontendUrl;

    @Override
    public void run(String... args) throws Exception {
        initializePayrollJobs();
    }

    /**
     * Initializes and schedules payroll jobs from the database at startup.
     */
    public void initializePayrollJobs() {
        List<Payroll> payrollList = payrollRepository.findAddWhereAutomaticIsTrue();

        payrollList.forEach(payroll -> {
            // Check if the job is already scheduled to avoid duplicates
            if (!scheduledFutureMap.containsKey(payroll.getPayrollId())) {
                schedulePayroll(payroll);
            } else {
                log.info("Payroll job {} is already scheduled, skipping.", payroll.getPayrollId());
            }
        });

        log.info("Initialized {} payroll jobs", payrollList.size());
    }

    /**
     * Schedules a payroll job based on its cron expression.
     * @param payroll The payroll entity to be scheduled.
     */
    public void schedulePayroll(Payroll payroll) {
        // Cancel any existing scheduled job for this payroll
        cancelScheduledPayroll(payroll.getPayrollId());

        if (payroll.getCronExpression() != null && !payroll.getCronExpression().isEmpty()) {
            try {
                log.info("Scheduling payroll job {} with cron expression: {}", payroll.getPayrollId(), payroll.getCronExpression());
                CronTrigger cronTrigger = new CronTrigger(payroll.getCronExpression());
                ScheduledFuture<?> scheduledTask = taskScheduler.schedule(() -> processPayroll(payroll), cronTrigger);
                scheduledFutureMap.put(payroll.getPayrollId(), scheduledTask);
            } catch (Exception e) {
                log.error("Invalid cron expression for payroll {}: {}", payroll.getPayrollId(), payroll.getCronExpression(), e);
            }
        }
    }

    /**
     * Cancels a scheduled payroll job.
     * @param payrollId The ID of the payroll job to cancel.
     */
    public void cancelScheduledPayroll(UUID payrollId) {
        ScheduledFuture<?> scheduledTask = scheduledFutureMap.get(payrollId);
        if (scheduledTask != null && !scheduledTask.isCancelled()) {
            scheduledTask.cancel(true);
            scheduledFutureMap.remove(payrollId);
            log.info("Cancelled payroll job with ID: {}", payrollId);
        }
    }

    /**
     * Processes a payroll job and updates its status.
     * @param payroll The payroll entity to process.
     */
    public DefaultApiResponse<?> processPayroll(Payroll payroll) {
        DefaultApiResponse<?> apiResponse = new DefaultApiResponse<>();
        try {
            payroll.setPayPeriodStart(LocalDate.now());
            payroll.setLastRunDate(LocalDate.now());

            // Calls the payment service to pay the employees in the payroll.
            DefaultApiResponse<BulkPayoutResponseDTO> request = paymentService.payEmployeesBulk(payroll.getPayrollId());

            apiResponse.setStatusCode(request.getStatusCode());
            apiResponse.setStatusMessage(request.getStatusMessage());

            payroll.setPaymentStatus(PaymentStatus.PAID);

            //====== Email Service ======//
            if (enableEmail){
                log.info("===== Email Enabled (payroll) =====");
                emailService.sendEmail(payroll.getCompany().getCompanyEmailAddress(),
                        "Payroll Just Ran",
                        createEmailContext(payroll.getCompany().getCompanyName(), frontendUrl, payroll.getPayrollId()),
                        "payrollRun");
            }
            else
                log.info("===== Email Disabled (payroll) =====");

            log.info("Successfully processed payroll with ID: {}", payroll.getPayrollId());
        } catch (Exception e) {
            payroll.setPaymentStatus(PaymentStatus.FAILED);
            log.error("Error processing payroll {}: {}", payroll.getPayrollId(), e.getMessage());
        } finally {
            payroll.setPayPeriodEnd(LocalDate.now());
            payrollRepository.save(payroll);

            // Clean up: remove completed job from map if no longer needed
            cancelScheduledPayroll(payroll.getPayrollId());
        }

        return apiResponse;
    }

    /**
     * Creates an email context for payroll notification.
     * @param companyName The name of the company.
     * @param frontendUrl The frontend URL for payroll details.
     * @param payrollId The payroll ID.
     * @return Thymeleaf email context with variables set.
     */
    private static Context createEmailContext(String companyName, String frontendUrl, UUID payrollId) {
        Context emailContext = new Context();
        emailContext.setVariable("username", companyName);
        emailContext.setVariable("paycraftURL", frontendUrl);
        emailContext.setVariable("payrollID", payrollId);
        return emailContext;
    }
}
