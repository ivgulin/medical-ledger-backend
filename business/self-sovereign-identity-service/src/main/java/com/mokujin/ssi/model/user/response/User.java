package com.mokujin.ssi.model.user.response;

import com.mokujin.ssi.model.government.Document;
import com.mokujin.ssi.model.internal.Contact;
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

    private String photo;

    private List<NationalDocument> nationalDocuments = new ArrayList<>();

    private List<Contact> contacts = new ArrayList<>();

    private List<Document> documents = new ArrayList<>();

    public void addNationalDocument(NationalDocument nationalDocument) {
        nationalDocuments.add(nationalDocument);
    }

    public void addContact(Contact contact) {
        contacts.add(contact);
    }

    public void addDocument(Document document) {
        documents.add(document);
    }
}
