package com.aalto.paycraft.entity;

import com.aalto.paycraft.dto.enums.PaymentStatus;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.JdbcTypeCode;
import java.math.BigDecimal;
import java.sql.Types;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
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

    @Column(nullable = false, unique = true)
    private String payrollName;

    @Builder.Default
    @Column(nullable = false)
    private Boolean automatic = false; // Indicates if the payroll is automatic

    private BigDecimal totalSalary; // The total salary for the payroll run (can be manually set)

    private LocalDate lastRunDate; // The date to run the payroll (can be null if manually run)

    private LocalDate payPeriodStart; // Start date of the pay period (optional for manual handling)

    private LocalDate payPeriodEnd; // End date of the pay period (optional for manual handling)

    private String cronExpression;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private PaymentStatus paymentStatus;

    @ManyToOne
    @JoinColumn(name = "companyId", referencedColumnName = "companyId", nullable = false)
    private Company company; // Reference to the company that owns this payroll

    @ManyToMany
    @JoinTable(
            name = "payroll_employee", // Name of the join table
            joinColumns = @JoinColumn(name = "payrollId"), // Join column for Payroll
            inverseJoinColumns = @JoinColumn(name = "employeeId") // Join column for Employee
    )
    private List<Employee> employees = new ArrayList<>(); // List of employees associated with the payroll
}
