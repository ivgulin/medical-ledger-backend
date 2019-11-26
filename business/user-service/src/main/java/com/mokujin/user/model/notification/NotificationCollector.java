package com.mokujin.user.model.notification;

import com.mokujin.user.model.notification.extention.*;
import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class NotificationCollector {

    private List<ChatNotification> messages;
    private List<SystemNotification> connections;
    private List<SystemNotification> invitations;
    private List<PresentationNotification> presentations;
    private List<ProofNotification> proofs;
    private List<HealthNotification> health;
    private List<OfferNotification> offers;
    private List<AskNotification> asks;
    private List<DocumentNotification> documents;

}
