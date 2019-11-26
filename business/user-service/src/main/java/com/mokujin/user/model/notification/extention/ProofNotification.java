package com.mokujin.user.model.notification.extention;

import com.mokujin.user.model.Contact;
import com.mokujin.user.model.notification.SystemNotification;
import com.mokujin.user.model.presentation.Proof;
import lombok.Data;
import lombok.EqualsAndHashCode;

import static com.mokujin.user.model.notification.Notification.Type.PROOF;

@Data
@EqualsAndHashCode(callSuper = true)
public class ProofNotification extends SystemNotification {

    private Proof proof;

    public ProofNotification(Long date, Contact contact, String titleEn, String titleUkr,
                             String contentEn, String contentUkr, Proof proof) {
        super(date, PROOF, contact, titleEn, titleUkr, contentEn, contentUkr);
        this.proof = proof;
    }
}
