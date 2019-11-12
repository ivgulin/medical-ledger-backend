package com.mokujin.government.service.impl;

import com.mokujin.government.model.dto.Person;
import com.mokujin.government.model.entity.KnownIdentity;
import com.mokujin.government.model.entity.NationalPassport;
import com.mokujin.government.model.exception.ResourceNotFoundException;
import com.mokujin.government.repository.KnownIdentityRepository;
import com.mokujin.government.service.FileService;
import com.mokujin.government.service.KnownIdentityService;
import jdk.nashorn.internal.ir.annotations.Ignore;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockMultipartFile;

import java.util.Collections;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class KnownIdentityServiceImplTest {

    @Mock
    private KnownIdentityRepository knownIdentityRepository;

    @Mock
    private FileService fileService;

    private KnownIdentityService knownIdentityService;


    @BeforeEach
    void setUp() {
        knownIdentityService = new KnownIdentityServiceImpl(knownIdentityRepository, fileService);
    }

    @Test
    void save_knownIdentityIsOk_savedKnownIdentityIsReturned() {
        KnownIdentity knownIdentity = KnownIdentity.builder()
                .nationalPassport(NationalPassport.builder().placesOfResidence(Collections.emptySet()).build())
                .build();
        when(knownIdentityRepository.save(knownIdentity)).thenReturn(knownIdentity);
        KnownIdentity savedKnownIdentity = knownIdentityService.save(knownIdentity);
        assertEquals(knownIdentity, savedKnownIdentity);
    }

    @Test
    void uploadPhoto_validInputs_updatedKnownIdentityIsReturned() {
        String fileName = "fileName";
        KnownIdentity knownIdentity = KnownIdentity.builder()
                .id(1)
                .nationalPassport(NationalPassport.builder().build())
                .build();
        MockMultipartFile file = new MockMultipartFile("successful_save", new byte[]{});
        when(knownIdentityRepository.findById(knownIdentity.getId())).thenReturn(Optional.of(knownIdentity));
        when(fileService.saveFile(file)).thenReturn(fileName);
        KnownIdentity savedKnownIdentity = knownIdentityService.uploadPhoto(knownIdentity.getId(), file);
        knownIdentity.getNationalPassport().setImageName(fileName);
        assertEquals(knownIdentity, savedKnownIdentity);
    }

    @Test
    void uploadPhoto_identityHasNotBeenFound_exceptionIsThrown() {
        Integer id = 1;
        when(knownIdentityRepository.findById(id)).thenReturn(Optional.empty());
        assertThrows(ResourceNotFoundException.class, () -> knownIdentityService.uploadPhoto(id, null));
    }

    @Test
    void get_identityHasNotBeenFound_exceptionIsThrown() {
        Integer id = 1;
        when(knownIdentityRepository.findById(id)).thenReturn(Optional.empty());
        assertThrows(ResourceNotFoundException.class, () -> knownIdentityService.get(id));
    }

    @Test
    @Ignore
        // TODO: 11/12/2019 complete
    void getWithImage_filterLeavesNoIdentity_exceptionIsThrown() {
        Person person = Person.builder()
                .nationalNumber("number")
                .fatherName("fathername")
                .firstName("first")
                .lastName("last")
                .build();
        when(knownIdentityRepository.findByNationalNumber_Number(person.getNationalNumber()))
                .thenReturn(Optional.empty());
        assertThrows(ResourceNotFoundException.class, () -> knownIdentityService.getWithImage(person));
    }

    @Test
    void getWithImage_identityHasNotBeenFound_exceptionIsThrown() {
        Person person = Person.builder()
                .nationalNumber("number")
                .build();
        when(knownIdentityRepository.findByNationalNumber_Number(person.getNationalNumber()))
                .thenReturn(Optional.empty());
        assertThrows(ResourceNotFoundException.class, () -> knownIdentityService.getWithImage(person));
    }


}