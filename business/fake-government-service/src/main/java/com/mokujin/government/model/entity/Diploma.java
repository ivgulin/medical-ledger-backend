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
@Table(name = "diploma")
public class Diploma {

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
    @Column(name = "place_of_study")
    private String placeOfStudy;

    @NotNull
    @Column(name = "course_of_study")
    private String courseOfStudy;

    @NotNull
    @Column(name = "date_of_issue")
    private Long dateOfIssue;

    @NotNull
    @Column(name = "qualification")
    private String qualification;

    @NotNull
    @Column(name = "issuer")
    private String issuer;
}
