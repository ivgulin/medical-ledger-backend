package com.mokujin.user.model.notification;

import com.mokujin.user.model.Contact;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.List;

@Data
@EqualsAndHashCode(callSuper = true)
public class PresentationNotification extends SystemNotification {

    private String documentType;

    private List<String> attributes;

    public PresentationNotification(Long date, Type type, Contact contact, String titleEn, String titleUkr,
                                    String contentEn, String contentUkr, String documentType, List<String> attributes) {
        super(date, type, contact, titleEn, titleUkr, contentEn, contentUkr);
        this.documentType = documentType;
        this.attributes = attributes;
    }
}
