package com.mokujin.government.model.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import javax.validation.constraints.NotNull;

@Data
@Entity
@NoArgsConstructor
@AllArgsConstructor
@Builder(toBuilder = true)
@Table(name = "known_identity")
public class KnownIdentity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @NotNull
    @OneToOne(cascade = CascadeType.ALL)
    @JoinColumn(name = "national_passport_id")
    private NationalPassport nationalPassport;

    @NotNull
    @OneToOne(cascade = CascadeType.ALL)
    @JoinColumn(name = "national_number")
    private NationalNumber nationalNumber;
}
