package com.mokujin.user.service.impl;

import com.mokujin.user.model.Contact;
import com.mokujin.user.model.ProcessedUserCredentials;
import com.mokujin.user.model.User;
import com.mokujin.user.model.document.Document;
import com.mokujin.user.model.document.impl.medical.dicom.MedicalImage;
import com.mokujin.user.model.document.impl.medical.hl7.Procedure;
import com.mokujin.user.model.exception.extention.ClientException;
import com.mokujin.user.model.exception.extention.ServerException;
import com.mokujin.user.model.internal.DocumentDraft;
import com.mokujin.user.model.internal.MedicalImageDraft;
import com.mokujin.user.model.internal.OfferRequest;
import com.mokujin.user.model.internal.ProcedureDraft;
import com.mokujin.user.service.NotificationService;
import com.mokujin.user.service.UserService;
import com.pixelmed.dicom.AttributeList;
import com.pixelmed.dicom.AttributeTag;
import com.pixelmed.dicom.TagFromName;
import lombok.SneakyThrows;
import mockit.MockUp;
import org.apache.tomcat.util.codec.binary.Base64;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.client.RestTemplate;

import javax.imageio.ImageIO;
import java.awt.image.RenderedImage;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class DocumentServiceImplTest {

    private final String RESOURCES_PATH = "src/test/resources/";
    @Mock
    private UserService userService;
    @Mock
    private NotificationService notificationService;
    @Mock
    private RestTemplate restTemplate;
    @InjectMocks
    private DocumentServiceImpl documentService;

    @Test
    void offerCredential_validInputs_userIsReturned() {

        String publicKey = "public";
        String privateKey = "private";

        User doctor = new User();
        doctor.setFirstName("name");

        when(userService.get(publicKey, privateKey)).thenReturn(doctor);

        documentService = spy(documentService);

        MedicalImageDraft draft = new MedicalImageDraft();
        MedicalImage medicalImage = new MedicalImage();

        doReturn(medicalImage).when(documentService).convertToDocument(draft);

        String patientNumber = "number";

        when(notificationService.addOfferNotification(publicKey, privateKey, doctor, medicalImage, patientNumber))
                .thenReturn(null);

        User result = documentService.offerCredential(publicKey, privateKey, draft, patientNumber);

        assertEquals(doctor, result);
        verify(notificationService, times(1))
                .addOfferNotification(publicKey, privateKey, doctor, medicalImage, patientNumber);
    }

    @Test
    void accept_validInputs_userIsReturned() {

        String publicKey = "public patient";
        String privateKey = "private patient";

        String patientNumber = "patient";
        String doctorNumber = "doctor";

        ProcessedUserCredentials doctorCredentials = ProcessedUserCredentials.builder()
                .publicKey("public doctor").privateKey("private doctor").build();

        when(notificationService.removeOfferNotification(patientNumber, doctorNumber))
                .thenReturn(doctorCredentials);

        User patient = new User();
        patient.setFirstName("name");

        Procedure procedure = new Procedure();

        ArgumentCaptor<OfferRequest> requestArgumentCaptor = ArgumentCaptor.forClass(OfferRequest.class);
        OfferRequest expectedOfferRequest = OfferRequest.builder()
                .document(procedure).doctorCredentials(doctorCredentials).build();

        when(restTemplate.postForObject(anyString(), requestArgumentCaptor.capture(), any())).thenReturn(patient);

        User result = documentService.accept(publicKey, privateKey, procedure, patientNumber, doctorNumber);

        assertEquals(patient, result);
        assertEquals(expectedOfferRequest, requestArgumentCaptor.getValue());
    }

    @Test
    void decline_validInputs_methodIsExecuted() {

        String patientNumber = "patient";
        String doctorNumber = "doctor";

        when(notificationService.removeOfferNotification(patientNumber, doctorNumber))
                .thenReturn(null);

        documentService.decline(patientNumber, doctorNumber);
        verify(notificationService, times(1)).removeOfferNotification(patientNumber, doctorNumber);
    }

    @Test
    void askDocument_validInputs_userIsReturned() {
        String doctorNumber = "doctor number";
        String patientNumber = "patient number";

        User doctor = new User();
        doctor.setNationalNumber(doctorNumber);

        String publicKey = "public";
        String privateKey = "private";

        List<String> keywords = Collections.singletonList("test");

        when(userService.get(publicKey, privateKey)).thenReturn(doctor);
        when(notificationService.addAskNotification(doctor, keywords, patientNumber)).thenReturn(null);

        User result = documentService.askDocument(publicKey, privateKey, keywords, patientNumber);

        assertEquals(doctor, result);
    }

    @Test
    void shareDocument_validInputs_userIsReturned() {
        String doctorNumber = "doctor number";
        String patientNumber = "patient number";

        User patient = new User();
        patient.setNationalNumber(patientNumber);

        String publicKey = "public";
        String privateKey = "private";

        MedicalImage medicalImage = new MedicalImage();

        when(userService.get(publicKey, privateKey)).thenReturn(patient);
        when(notificationService.addDocumentNotification(patient, medicalImage, doctorNumber)).thenReturn(null);

        User result = documentService.shareDocument(publicKey, privateKey, medicalImage, doctorNumber);

        assertEquals(patient, result);
    }

    @Test
    void convertToDocument_validProcedureDraft_procedureIsReturned() {

        ProcedureDraft draft = new ProcedureDraft();

        documentService = spy(documentService);

        Procedure procedure = new Procedure();
        doReturn(procedure).when(documentService).convertToProcedure(draft);

        Document result = documentService.convertToDocument(draft);

        assertEquals(procedure, result);
    }

    @Test
    void convertToDocument_validMedicalImageDraft_medicalImageIsReturned() {

        MedicalImageDraft draft = new MedicalImageDraft();

        documentService = spy(documentService);

        MedicalImage medicalImage = new MedicalImage();
        doReturn(medicalImage).when(documentService).convertToMedicalImage(draft);

        Document result = documentService.convertToDocument(draft);

        assertEquals(medicalImage, result);
    }

    @Test
    void convertToDocument_unknownDraft_exceptionIsThrown() {

        assertThrows(ClientException.class, () -> documentService.convertToDocument(new DocumentDraft("test")));
    }

    @Test
    @SneakyThrows
    void convertToProcedure_validDraft_procedureIsReturned() {

        String name = "name";
        ProcedureDraft.Status status = ProcedureDraft.Status.completed;
        String test = "test";
        String number = "number";
        long date = 1575542197000L;
        String reason = "reason";


        ProcedureDraft draft = new ProcedureDraft();
        draft.setName(name);
        draft.setStatus(status);
        draft.setNotDoneReason(test);
        draft.setDescription(test);
        draft.setPatient(Contact.builder().contactName(name).nationalNumber(number).build());
        draft.setStartDate(date);
        draft.setRecorder(name);
        draft.setAsserter(name);
        draft.setPerformer(name);
        draft.setReason(reason);

        Procedure result = documentService.convertToProcedure(draft);

        assertEquals(status.name(), result.getStatus());
        assertEquals(test, result.getNotDoneReason().getText());
        assertEquals(test, result.getCode().getText());
        assertEquals(name, result.getSubject().getReference());
        assertEquals(number, result.getSubject().getDisplay());
        assertEquals("2019-12-05", result.getPerformedDateTime());

        draft.setEndDate(date);
        result = documentService.convertToProcedure(draft);
        assertEquals("2019-12-05", result.getPerformedPeriod().getStart());
        assertEquals("2019-12-05", result.getPerformedPeriod().getEnd());

    }

    @Test
    @SneakyThrows
    void convertToMedicalImage_validDraft_medicalImageIsReturned() {

        MedicalImageDraft draft = new MedicalImageDraft();

        File file = new File(RESOURCES_PATH + "DICOM_Image.dcm");
        FileInputStream fileInputStreamReader = new FileInputStream(file);
        byte[] bytes = new byte[(int) file.length()];
        fileInputStreamReader.read(bytes);
        String encoded = new String(Base64.encodeBase64(bytes), StandardCharsets.UTF_8);

        draft.setImage(encoded);

        MedicalImage result = documentService.convertToMedicalImage(draft);

        assertEquals("CLINICAL BRAIN", result.getAttributes().get("ProtocolName"));
        assertEquals("1.00000\\0.00000\\0.00000\\0.00000\\0.990960\\0.134158", result.getAttributes().get("ImageOrientationPatient"));
        assertEquals("16", result.getAttributes().get("BitsAllocated"));
        assertEquals("20010323", result.getAttributes().get("ContentDate"));
        assertEquals("HFS", result.getAttributes().get("PatientPosition"));
        assertEquals("028Y", result.getAttributes().get("PatientAge"));
        assertEquals("L\\PH", result.getAttributes().get("PatientOrientation"));
    }

    @Test
    @SneakyThrows
    void convertToMedicalImage_exceptionIsThrownDuringEncryption_exceptionIsThrown() {

        MedicalImageDraft draft = new MedicalImageDraft();

        File file = new File(RESOURCES_PATH + "DICOM_Image.dcm");
        FileInputStream fileInputStreamReader = new FileInputStream(file);
        byte[] bytes = new byte[(int) file.length()];
        fileInputStreamReader.read(bytes);
        String encoded = new String(Base64.encodeBase64(bytes), StandardCharsets.UTF_8);

        draft.setImage(encoded);

        documentService = spy(documentService);
        new MockUp<ImageIO>() {
            @mockit.Mock
            public boolean write(RenderedImage im, String formatName, OutputStream output) throws IOException {
                throw new IOException();
            }
        };
        assertThrows(ServerException.class, () -> documentService.convertToMedicalImage(draft));
    }

    @Test
    @SneakyThrows
    void getTagInformation_validInputs_stringIsReturned() {

        AttributeTag tag = TagFromName.ProtocolName;

        AttributeList list = new AttributeList();
        list.read(RESOURCES_PATH + "DICOM_Image.dcm");

        String result = documentService.getTagInformation(tag, list);

        assertEquals("CLINICAL BRAIN", result);
    }
}