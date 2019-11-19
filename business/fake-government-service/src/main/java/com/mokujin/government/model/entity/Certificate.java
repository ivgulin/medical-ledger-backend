package com.mokujin.government.model.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.*;

import javax.persistence.*;
import javax.validation.constraints.NotNull;

@Data
@Entity
@NoArgsConstructor
@AllArgsConstructor
@Builder(toBuilder = true)
@Table(name = "certificate")
public class Certificate {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @NotNull
    @Column(name = "number")
    private String number;

    @NotNull
    @Column(name = "first_name")
    private String firstName;

    @NotNull
    @Column(name = "last_name")
    private String lastName;

    @NotNull
    @Column(name = "father_name")
    private String fatherName;

    @NotNull
    @Column(name = "date_of_exam")
    private Long dateOfExam;

    @NotNull
    @Column(name = "date_of_issue")
    private Long dateOfIssue;

    @NotNull
    @Column(name = "qualification")
    private String qualification;

    @NotNull
    @Column(name = "course_of_study")
    private String courseOfStudy;

    @NotNull
    @Column(name = "category")
    @Enumerated(EnumType.STRING)
    private Category category;

    @NotNull
    @Column(name = "expires_in")
    private Long expiresIn;

    @NotNull
    @Column(name = "issuer")
    private String issuer;

    @JsonIgnore
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "known_identity_id")
    private KnownIdentity knownIdentity;

    public enum Category{
        I, II, HIGHER
    }

}
