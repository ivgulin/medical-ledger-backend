package com.mokujin.user.model.notification;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.io.Serializable;

@Data
@AllArgsConstructor
public abstract class Notification implements Serializable {

    private Long date;

    private Type type;

    public enum Type {
        MESSAGE,
        CONNECTION,
        INVITATION,
        PRESENTATION,
        PROOF,
        HEALTH,
        OFFER,
        ASK,
        DOCUMENT
    }

}
