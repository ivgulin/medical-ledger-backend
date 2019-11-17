package com.mokujin.user.service;

import com.mokujin.user.model.User;

public interface InvitationService {

    User inviteBack(String publicKey, String privateKey, String invitorNumber);

    User accept(String publicKey, String privateKey, String nationalNumber);

    void decline(String nationalNumber);
}
