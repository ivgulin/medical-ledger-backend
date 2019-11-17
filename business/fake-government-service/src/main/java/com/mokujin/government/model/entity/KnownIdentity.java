package com.mokujin.government.model.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import javax.validation.constraints.NotNull;
import java.util.ArrayList;
import java.util.List;

import static javax.persistence.CascadeType.ALL;

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
    @Column(name = "role")
    @Enumerated(EnumType.STRING)
    private Role role;

    @NotNull
    @OneToOne(cascade = CascadeType.ALL)
    @JoinColumn(name = "national_passport_id")
    private NationalPassport nationalPassport;

    @NotNull
    @OneToOne(cascade = CascadeType.ALL)
    @JoinColumn(name = "national_number")
    private NationalNumber nationalNumber;

    @OneToOne(cascade = CascadeType.ALL)
    @JoinColumn(name = "diploma_id")
    private Diploma diploma;

    @OneToMany(mappedBy = "knownIdentity", cascade = ALL)
    private List<Certificate> certificates = new ArrayList<>();

    public void addCertificate(Certificate certificate) {
        certificate.setKnownIdentity(this);
        certificates.add(certificate);
    }

    public void removeCertificate(Certificate certificate) {
        certificate.setKnownIdentity(null);
        certificates.remove(certificate);
    }

    public enum Role {
        PATIENT,
        DOCTOR
    }
}
