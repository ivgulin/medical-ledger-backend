package com.mokujin.user.service;

import com.mokujin.user.model.Contact;
import com.mokujin.user.model.User;

public interface InvitationService {

    User inviteBack(String publicKey, String privateKey, Contact doctor);

    User accept(String publicKey, String privateKey, String doctorNumber, String patientNumber);

    void decline(String doctorNumber, String patientNumber);
}
