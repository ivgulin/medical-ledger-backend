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
@Table(name = "address")
public class Address {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @NotNull
    @Column(name = "administrative_area")
    private String administrativeArea;

    @NotNull
    private String street;

    @NotNull
    @Column(name = "street_number")
    private String streetNumber;

    @NotNull
    @Column(name = "apartment_number")
    private String apartmentNumber;

}
