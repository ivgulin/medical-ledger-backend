package com.mokujin.government.service.impl;

import com.mokujin.government.model.dto.*;
import com.mokujin.government.model.entity.*;
import com.mokujin.government.model.exception.extention.ResourceNotFoundException;
import com.mokujin.government.repository.KnownIdentityRepository;
import com.mokujin.government.service.FileService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockMultipartFile;

import java.util.*;
import java.util.stream.Collectors;
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

    @Test
    void save_knownIdentityIsOk_savedKnownIdentityIsReturned() {

        Set<PlaceOfResidence> placesOfResidence = new HashSet<>();
        PlaceOfResidence placeOfResidenceOne = PlaceOfResidence.builder()
                .startDate(123L)
                .endDate(234L)
                .build();
        PlaceOfResidence placeOfResidenceTwo = PlaceOfResidence.builder()
                .startDate(345L)
                .endDate(456L)
                .build();
        placesOfResidence.add(placeOfResidenceOne);
        placesOfResidence.add(placeOfResidenceTwo);

        List<Certificate> certificates = new ArrayList<>();
        Certificate dentistCertificate = Certificate.builder()
                .qualification("dentist")
                .build();
        Certificate surgeonCertificate = Certificate.builder()
                .qualification("surgeon")
                .build();
        certificates.add(dentistCertificate);
        certificates.add(surgeonCertificate);

        NationalPassport passport = NationalPassport.builder()
                .placesOfResidence(placesOfResidence)
                .build();

        KnownIdentity knownIdentity = KnownIdentity.builder()
                .nationalPassport(passport)
                .certificates(certificates)
                .build();
        when(knownIdentityRepository.save(knownIdentity)).thenReturn(knownIdentity);
        KnownIdentity savedKnownIdentity = knownIdentityService.save(knownIdentity);

        placeOfResidenceOne.setNationalPassport(passport);
        placeOfResidenceTwo.setNationalPassport(passport);
        dentistCertificate.setKnownIdentity(knownIdentity);
        surgeonCertificate.setKnownIdentity(knownIdentity);

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
    void getAll_identitiesExists_iterableIsReturned() {
        ArrayList<KnownIdentity> expected = new ArrayList<>();
        expected.add(KnownIdentity.builder().id(1).build());
        when(knownIdentityRepository.findAll()).thenReturn(expected);

        Iterable<KnownIdentity> result = knownIdentityService.getAll();

        assertEquals(expected, result);
    }

    @Test
    void getWithImage_everyDocumentInThePlace_knownIdentityIsReturned() {
        String nationalNumberValue = "number";
        String fatherName = "fathername";
        String firstName = "first";
        String lastName = "last";
        long dateOfBirth = 1234567L;
        String imageName = "test";
        String qualification = "surgeon";

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

        Diploma diploma = Diploma.builder()
                .firstName(firstName)
                .lastName(lastName)
                .fatherName(fatherName)
                .qualification(qualification)
                .build();

        List<Certificate> certificates = new ArrayList<>();
        Certificate certificateOne = Certificate.builder()
                .qualification(qualification)
                .build();

        Certificate certificateTwo = Certificate.builder()
                .lastName(lastName)
                .build();
        certificates.add(certificateOne);
        certificates.add(certificateTwo);

        KnownIdentity knownIdentity = KnownIdentity.builder()
                .nationalPassport(nationalPassport)
                .nationalNumber(nationalNumber)
                .diploma(diploma)
                .certificates(certificates)
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

        DiplomaDTO expectedDiploma = new DiplomaDTO(diploma);
        expected.setDiploma(expectedDiploma);

        List<Certificate> expectedCertificates = certificates.stream()
                .map(CertificateDTO::new)
                .collect(Collectors.toList());
        expected.setCertificates(expectedCertificates);

        KnownIdentityDTO result = knownIdentityService.getWithImage(person);

        assertEquals(expected, result);
    }

    @Test
    void getWithImage_noDiplomaOrCertificateExists_knownIdentityIsReturned() {
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
}