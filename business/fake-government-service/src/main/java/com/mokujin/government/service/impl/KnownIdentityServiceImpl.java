package com.mokujin.government.service.impl;

import com.mokujin.government.model.dto.KnownIdentityDTO;
import com.mokujin.government.model.dto.NationalNumberDTO;
import com.mokujin.government.model.dto.NationalPassportDTO;
import com.mokujin.government.model.dto.Person;
import com.mokujin.government.model.entity.KnownIdentity;
import com.mokujin.government.model.entity.NationalPassport;
import com.mokujin.government.model.exception.extention.ResourceNotFoundException;
import com.mokujin.government.repository.KnownIdentityRepository;
import com.mokujin.government.service.FileService;
import com.mokujin.government.service.KnownIdentityService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

@Slf4j
@Service
@Transactional
@RequiredArgsConstructor
public class KnownIdentityServiceImpl implements KnownIdentityService {

    private final KnownIdentityRepository knownIdentityRepository;

    private final FileService fileService;

    @Override
    public KnownIdentity save(KnownIdentity knownIdentity) {

        NationalPassport nationalPassport = knownIdentity.getNationalPassport();
        nationalPassport.getPlacesOfResidence().forEach(nationalPassport::addPlaceOfResidence);

        return knownIdentityRepository.save(knownIdentity);
    }

    @Override
    public KnownIdentity uploadPhoto(Integer identityId, MultipartFile multipartFile) {

        KnownIdentity knownIdentity = knownIdentityRepository.findById(identityId)
                .orElseThrow(() -> new ResourceNotFoundException("Identity hasn't been found."));
        log.info("knownIdentity has been found in uploadPhoto: '{}'", knownIdentity);

        String fileName = fileService.saveFile(multipartFile);

        knownIdentity.getNationalPassport().setImageName(fileName);

        return knownIdentity;
    }

    @Override
    public KnownIdentity get(Integer id) {
        return knownIdentityRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Identity hasn't been found."));
    }

    @Override
    public KnownIdentityDTO getWithImage(Person person) {
        KnownIdentityDTO knownIdentity = new KnownIdentityDTO(knownIdentityRepository
                .findByNationalNumber_Number(person.getNationalNumber())
                .filter(i -> i.getNationalPassport().getFirstName().equals(person.getFirstName())
                        && i.getNationalPassport().getLastName().equals(person.getLastName())
                        && i.getNationalPassport().getFatherName().equals(person.getFatherName())
                        && i.getNationalPassport().getDateOfBirth().equals(person.getDateOfBirth()))
                .orElseThrow(() -> new ResourceNotFoundException("Identity hasn't been found.")));
        log.info("knownIdentity has been found in getWithImage: '{}'", knownIdentity);

        NationalPassportDTO nationalPassport = new NationalPassportDTO(knownIdentity.getNationalPassport());
        String base64EncodedImage = fileService.getBase64EncodedFile(nationalPassport.getImageName());
        nationalPassport.setImage(base64EncodedImage);
        knownIdentity.setNationalPassport(nationalPassport);

        NationalNumberDTO nationalNumber = new NationalNumberDTO(knownIdentity.getNationalNumber());
        knownIdentity.setNationalNumber(nationalNumber);

        return knownIdentity;
    }
}
