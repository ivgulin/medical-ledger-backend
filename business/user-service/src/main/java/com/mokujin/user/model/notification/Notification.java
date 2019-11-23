package com.mokujin.user.model.notification;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public abstract class Notification {

    private Long date;

    private Type type;

    public enum Type {
        MESSAGE,
        CONNECTION,
        INVITATION,
        PRESENTATION,
        PROOF,
        HEALTH
    }

}
