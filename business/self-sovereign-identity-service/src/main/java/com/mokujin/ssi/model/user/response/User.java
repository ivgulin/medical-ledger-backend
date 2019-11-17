package com.mokujin.ssi.model.user.response;

import com.mokujin.ssi.model.internal.Contact;
import com.mokujin.ssi.model.internal.Credential;
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

    private String firstName;

    private String lastName;

    private String fatherName;

    private String nationalNumber;

    private String photo;

    private List<Credential> nationalCredentials = new ArrayList<>();

    private List<Contact> contacts = new ArrayList<>();

    private List<Credential> credentials = new ArrayList<>();

    public void addNationalCredential(Credential nationalCredential) {
        nationalCredentials.add(nationalCredential);
    }

    public void addContact(Contact contact) {
        contacts.add(contact);
    }

    public void addCredential(Credential credential) {
        credentials.add(credential);
    }
}
