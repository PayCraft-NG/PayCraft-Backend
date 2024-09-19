package com.aalto.paycraft.entity;

import com.aalto.paycraft.dto.enumeration.PaymentStatus;
import com.aalto.paycraft.dto.enumeration.PayrollFrequency;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;

import java.math.BigDecimal;
import java.sql.Types;
import java.time.LocalDate;
import java.util.UUID;

@Builder
@Entity
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@ToString
@Table(name = "Payroll")
public class Payroll extends BaseEntity {

    @Id
    @GeneratedValue
    @JdbcTypeCode(Types.VARCHAR)
    private UUID payrollId; // The unique identifier for the payroll

    @Column(nullable = false)
    private Boolean automatic; // Indicates if the payroll is automatic

    private BigDecimal totalSalary; // The total salary for the payroll run (can be manually set)

    private LocalDate lastRunDate; // The date to run the payroll (can be null if manually run)

    private LocalDate payPeriodStart; // Start date of the pay period (optional for manual handling)

    private LocalDate payPeriodEnd; // End date of the pay period (optional for manual handling)

    @Enumerated(EnumType.STRING)
    private PayrollFrequency frequency; // The frequency of payroll execution (optional if manual)

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private PaymentStatus paymentStatus; // The status of the payroll (e.g., Pending, Completed)

    @ManyToOne
    @JoinColumn(name = "companyId", referencedColumnName = "companyId", nullable = false)
    private Company company; // Reference to the company that owns this payroll
}
