package com.mokujin.user.model;

import com.mokujin.user.model.government.NationalNumber;
import com.mokujin.user.model.government.NationalPassport;
import lombok.Data;

@Data
public class User {

    private String firstName;

    private String lastName;

    private String fatherName;

    private String image;

    private NationalPassport nationalPassport;

    private NationalNumber nationalNumber;

}
