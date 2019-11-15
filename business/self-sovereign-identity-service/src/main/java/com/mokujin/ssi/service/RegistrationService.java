package com.mokujin.ssi.service;

import com.mokujin.ssi.model.user.request.UserRegistrationDetails;
import com.mokujin.ssi.model.user.response.User;

public interface RegistrationService {

    User register(UserRegistrationDetails credentials, String publicKey, String privateKey);

}
