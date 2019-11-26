package com.mokujin.user.model.notification.extention;

import com.mokujin.user.model.Contact;
import com.mokujin.user.model.notification.SystemNotification;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.List;

import static com.mokujin.user.model.notification.Notification.Type.*;

@Data
@EqualsAndHashCode(callSuper = true)
public class PresentationNotification extends SystemNotification {

    private String documentType;

    private List<String> attributes;

    public PresentationNotification(Long date, Contact contact, String titleEn, String titleUkr,
                                    String contentEn, String contentUkr, String documentType, List<String> attributes) {
        super(date, PRESENTATION, contact, titleEn, titleUkr, contentEn, contentUkr);
        this.documentType = documentType;
        this.attributes = attributes;
    }
}
