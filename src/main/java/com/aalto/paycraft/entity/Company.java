package com.aalto.paycraft.entity;

import com.aalto.paycraft.dto.enums.Currency;
import com.aalto.paycraft.dto.enums.CompanySize;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.JdbcTypeCode;

import java.sql.Types;
import java.util.UUID;

@Entity
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@ToString
@Table(name = "Company")
public class Company extends BaseEntity {
    @Id
    @GeneratedValue
    @JdbcTypeCode(Types.VARCHAR)
    private UUID companyId;

    @Column(nullable = false, length = 100)
    private String companyName;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 10)
    private CompanySize companySize;

    @Column(nullable = false, length = 100)
    private String companyEmailAddress;

    @Column(nullable = false, length = 100)
    private String companyPhoneNumber;

    @Column(nullable = false, length = 100)
    private String companyStreetAddress;

    @Column(nullable = false, length = 100)
    private String companyCountry;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 10)
    private Currency companyCurrency;

    @ManyToOne
    @JoinColumn(name = "employerId", referencedColumnName = "employerId", nullable = false)
    private Employer employer;
}
