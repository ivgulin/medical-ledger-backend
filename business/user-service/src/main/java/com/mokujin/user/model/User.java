package com.mokujin.user.model;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
public class User {

    private String firstName;

    private String lastName;

    private String fatherName;

    private String photo;

    private List<Credential> nationalCredentials = new ArrayList<>();

    private List<Contact> contacts = new ArrayList<>();

    private List<Credential> credentials = new ArrayList<>();

}
