package com.mokujin.user.service;

import com.mokujin.user.model.ProcessedUserCredentials;
import com.mokujin.user.model.User;
import com.mokujin.user.model.UserCredentials;
import com.mokujin.user.model.UserRegistrationDetails;

public interface RegistrationService {

    ProcessedUserCredentials createWallet(UserCredentials userCredentials);

    User registerUser(UserRegistrationDetails userRegistrationDetails, String publicKey, String privateKey);

}
