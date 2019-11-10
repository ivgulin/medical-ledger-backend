package com.mokujin.ssi.service;

import com.mokujin.ssi.model.government.KnownIdentity;
import com.mokujin.ssi.model.user.request.UserRegistrationDetails;

public interface ValidationService {

    KnownIdentity validateNewbie(UserRegistrationDetails details);

}
