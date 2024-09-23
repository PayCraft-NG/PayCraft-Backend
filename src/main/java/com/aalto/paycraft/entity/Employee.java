package com.aalto.paycraft.entity;

import com.aalto.paycraft.dto.enums.Currency;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.JdbcTypeCode;

import java.math.BigDecimal;
import java.sql.Types;
import java.time.LocalDateTime;
import java.util.Date;
import java.util.List;
import java.util.UUID;

@Entity @Getter @Setter
@AllArgsConstructor @NoArgsConstructor
@ToString @Builder
@Table(name = "Employee")
public class Employee extends BaseEntity{
    @Id
    @GeneratedValue
    @JdbcTypeCode(Types.VARCHAR)
    private UUID employeeId;

    @Column(nullable = false, length = 100)
    private String firstName;

    @Column(nullable = false, length = 100)
    private String lastName;

    @Temporal(TemporalType.DATE)
    @Column(nullable = false)
    private Date dateOfBirth;

    @Column(nullable = false, length = 100, unique = true)
    private String emailAddress;

    @Column(nullable = false, length = 100, unique = true)
    private String phoneNumber;

    @Column(nullable = false, length = 100)
    private String streetAddress;

    @Column(nullable = false, length = 100)
    private String jobTitle;

    @Column(nullable = false, length = 100)
    private String department;

    @Column(nullable = false, length = 100, unique = true)
    private String bvn;

    @Column(nullable = false, length = 100)
    private String bankName;

    @Column(nullable = false, length = 100)
    private String accountNumber;

    @Column(nullable = false, precision = 15, scale = 2)
    private BigDecimal salaryAmount;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 10)
    private Currency salaryCurrency;

    @ManyToOne
    @JoinColumn(name = "companyId", nullable = false)
    private Company company;

    @ManyToMany(mappedBy = "employees")
    private List<Payroll> payrolls;

    @Builder.Default
    @Column(nullable = false)
    private boolean deleted = false;  // soft delete flag

    @Column
    private LocalDateTime deletedAt;

    public void setDeleted(boolean deleted) {
        this.deleted = deleted;
        this.deletedAt = deleted ? LocalDateTime.now() : null;  // Set timestamp when deleted
    }
}
