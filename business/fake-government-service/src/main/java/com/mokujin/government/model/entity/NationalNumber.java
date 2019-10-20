package com.mokujin.government.model.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.validation.constraints.NotNull;
import java.time.LocalDate;

@Data
@Entity
@NoArgsConstructor
@AllArgsConstructor
@Builder(toBuilder = true)
@Table(name = "national_number")
public class NationalNumber {

    @Id
    @NotNull
    private String number;

    @NotNull
    @Column(name = "registration_date")
    private LocalDate registrationDate;

    @NotNull
    private String issuer;

}
