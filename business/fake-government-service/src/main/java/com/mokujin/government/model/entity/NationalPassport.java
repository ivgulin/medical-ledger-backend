package com.mokujin.government.model.entity;

import lombok.*;

import javax.persistence.*;
import javax.validation.constraints.NotNull;
import java.util.HashSet;
import java.util.Set;

import static javax.persistence.CascadeType.ALL;

@Data
@Entity
@NoArgsConstructor
@AllArgsConstructor
@Builder(toBuilder = true)
@Table(name = "national_passport")
public class NationalPassport {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

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
    @Column(name = "date_of_birth")
    private Long dateOfBirth;

    @NotNull
    @Column(name = "place_of_birth")
    private String placeOfBirth;

    @Column(name = "image_path")
    private String imageName;

    @NotNull
    private String sex;

    @NotNull
    private String issuer;

    @NotNull
    @Column(name = "date_of_issue")
    private Long dateOfIssue;

    @NotNull
    @Setter(AccessLevel.PRIVATE)
    @OneToMany(mappedBy = "nationalPassport", cascade = ALL)
    private Set<PlaceOfResidence> placesOfResidence = new HashSet<>();

    public void addPlaceOfResidence(PlaceOfResidence placeOfResidence) {
        placeOfResidence.setNationalPassport(this);
        placesOfResidence.add(placeOfResidence);
    }

    public void removePlaceOfResidence(PlaceOfResidence placeOfResidence) {
        placeOfResidence.setNationalPassport(null);
        placesOfResidence.remove(placeOfResidence);
    }

}
