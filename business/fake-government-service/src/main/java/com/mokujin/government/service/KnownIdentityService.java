package com.mokujin.government.service;

import com.mokujin.government.model.dto.KnownIdentityDTO;
import com.mokujin.government.model.dto.Person;
import com.mokujin.government.model.entity.KnownIdentity;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public interface KnownIdentityService {

    KnownIdentity save(KnownIdentity knownIdentity);

    KnownIdentity uploadPhoto(Integer userId, MultipartFile multipartFile);

    KnownIdentity get(Integer id);

    Iterable<KnownIdentity> getAll();

    KnownIdentityDTO getWithImage(Person person);

}
