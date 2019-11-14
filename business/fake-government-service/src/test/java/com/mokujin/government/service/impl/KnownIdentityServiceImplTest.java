package com.mokujin.government.service.impl;

import com.mokujin.government.model.dto.KnownIdentityDTO;
import com.mokujin.government.model.dto.NationalNumberDTO;
import com.mokujin.government.model.dto.NationalPassportDTO;
import com.mokujin.government.model.dto.Person;
import com.mokujin.government.model.entity.KnownIdentity;
import com.mokujin.government.model.entity.NationalNumber;
import com.mokujin.government.model.entity.NationalPassport;
import com.mokujin.government.model.exception.ResourceNotFoundException;
import com.mokujin.government.repository.KnownIdentityRepository;
import com.mokujin.government.service.FileService;
import com.mokujin.government.service.KnownIdentityService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockMultipartFile;

import java.util.Collections;
import java.util.Optional;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class KnownIdentityServiceImplTest {

    @Mock
    private KnownIdentityRepository knownIdentityRepository;

    @Mock
    private FileService fileService;

    @InjectMocks
    private KnownIdentityServiceImpl knownIdentityService;

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
    void getWithImage_personIsOk_knownIdentityIsReturned() {
        String nationalNumberValue = "number";
        String fatherName = "fathername";
        String firstName = "first";
        String lastName = "last";
        long dateOfBirth = 1234567L;
        String imageName = "test";

        Person person = Person.builder()
                .nationalNumber(nationalNumberValue)
                .fatherName(fatherName)
                .firstName(firstName)
                .lastName(lastName)
                .dateOfBirth(dateOfBirth)
                .build();

        NationalPassport nationalPassport = NationalPassport.builder()
                .fatherName(fatherName)
                .firstName(firstName)
                .lastName(lastName)
                .dateOfBirth(dateOfBirth)
                .imageName(imageName)
                .build();

        NationalNumber nationalNumber = NationalNumber.builder()
                .number(nationalNumberValue)
                .build();

        KnownIdentity knownIdentity = KnownIdentity.builder()
                .nationalPassport(nationalPassport)
                .nationalNumber(nationalNumber)
                .build();

        when(knownIdentityRepository.findByNationalNumber_Number(person.getNationalNumber()))
                .thenReturn(Optional.of(knownIdentity));
        String encodedImageValue = "test";
        when(fileService.getBase64EncodedFile(imageName)).thenReturn(encodedImageValue);

        KnownIdentityDTO expected = new KnownIdentityDTO(knownIdentity);
        NationalPassportDTO expectedPassport = new NationalPassportDTO(nationalPassport);
        expectedPassport.setImage(encodedImageValue);
        expected.setNationalPassport(expectedPassport);
        NationalNumberDTO expectedNationalNumber = new NationalNumberDTO(nationalNumber);
        expected.setNationalNumber(expectedNationalNumber);

        KnownIdentityDTO result = knownIdentityService.getWithImage(person);

        assertEquals(expected, result);
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

    @ParameterizedTest
    @MethodSource("providePersonsAndPassports")
    void getWithImage_filterLeavesNoIdentity_exceptionIsThrown(Person person, NationalPassport nationalPassport) {

        KnownIdentity knownIdentity = KnownIdentity.builder()
                .nationalPassport(nationalPassport)
                .build();

        when(knownIdentityRepository.findByNationalNumber_Number(person.getNationalNumber()))
                .thenReturn(Optional.of(knownIdentity));
        assertThrows(ResourceNotFoundException.class, () -> knownIdentityService.getWithImage(person));
    }

    private static Stream<Arguments> providePersonsAndPassports() {

        String nationalNumber = "number";
        String fatherName = "fathername";
        String firstName = "first";
        String lastName = "last";
        long dateOfBirth = 1234567L;

        return Stream.of(
                Arguments.of(
                        Person.builder()
                                .nationalNumber(nationalNumber)
                                .fatherName(fatherName)
                                .firstName(firstName)
                                .lastName(lastName)
                                .dateOfBirth(dateOfBirth)
                                .build()
                        ,
                        NationalPassport.builder()
                                .fatherName(fatherName)
                                .firstName("another name")
                                .lastName(lastName)
                                .dateOfBirth(dateOfBirth)
                                .build()
                ),
                Arguments.of(
                        Person.builder()
                                .nationalNumber(nationalNumber)
                                .fatherName(fatherName)
                                .firstName(firstName)
                                .lastName(lastName)
                                .dateOfBirth(dateOfBirth)
                                .build()
                        ,
                        NationalPassport.builder()
                                .fatherName(fatherName)
                                .firstName(firstName)
                                .lastName("another name")
                                .dateOfBirth(dateOfBirth)
                                .build()
                ), Arguments.of(
                        Person.builder()
                                .nationalNumber(nationalNumber)
                                .fatherName(fatherName)
                                .firstName(firstName)
                                .lastName(lastName)
                                .dateOfBirth(dateOfBirth)
                                .build()
                        ,
                        NationalPassport.builder()
                                .fatherName("another name")
                                .firstName(firstName)
                                .lastName(lastName)
                                .dateOfBirth(dateOfBirth)
                                .build()
                ), Arguments.of(
                        Person.builder()
                                .nationalNumber(nationalNumber)
                                .fatherName(fatherName)
                                .firstName(firstName)
                                .lastName(lastName)
                                .dateOfBirth(dateOfBirth)
                                .build()
                        ,
                        NationalPassport.builder()
                                .fatherName(fatherName)
                                .firstName(firstName)
                                .lastName(lastName)
                                .dateOfBirth(11111111L)
                                .build()
                )
        );
    }
}