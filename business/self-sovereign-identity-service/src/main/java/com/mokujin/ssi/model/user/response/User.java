package com.mokujin.ssi.model.user.response;

import com.mokujin.ssi.model.internal.Contact;
import com.mokujin.ssi.model.internal.Credential;
import com.mokujin.ssi.model.internal.Role;
import com.mokujin.ssi.model.record.HealthRecord;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

import java.util.ArrayList;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Accessors(chain = true)
@Builder(toBuilder = true)
public class User {

    private Role role;

    private String firstName;

    private String lastName;

    private String fatherName;

    private String nationalNumber;

    private String photo;

    private List<Credential> nationalCredentials;

    private List<Contact> contacts;

    private List<Credential> credentials;

    private List<HealthRecord> records;
}
