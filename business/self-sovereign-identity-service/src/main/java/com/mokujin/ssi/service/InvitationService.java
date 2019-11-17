package com.mokujin.ssi.service;

import com.mokujin.ssi.model.user.request.UserCredentials;
import com.mokujin.ssi.model.user.response.User;

public interface InvitationService {

    User connect(String publicKey, String privateKey, UserCredentials userCredentials);

}
