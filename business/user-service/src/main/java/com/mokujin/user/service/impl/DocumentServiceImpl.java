package com.mokujin.user.service.impl;

import com.mokujin.user.model.ProcessedUserCredentials;
import com.mokujin.user.model.User;
import com.mokujin.user.model.document.Document;
import com.mokujin.user.model.document.impl.medical.dicom.MedicalImage;
import com.mokujin.user.model.document.impl.medical.hl7.Procedure;
import com.mokujin.user.model.document.impl.medical.hl7.component.*;
import com.mokujin.user.model.exception.extention.ClientException;
import com.mokujin.user.model.exception.extention.ServerException;
import com.mokujin.user.model.internal.DocumentDraft;
import com.mokujin.user.model.internal.MedicalImageDraft;
import com.mokujin.user.model.internal.OfferRequest;
import com.mokujin.user.model.internal.ProcedureDraft;
import com.mokujin.user.model.notification.Notification;
import com.mokujin.user.service.DocumentService;
import com.mokujin.user.service.NotificationService;
import com.mokujin.user.service.UserService;
import com.pixelmed.dicom.*;
import com.pixelmed.display.ConsumerFormatImageMaker;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.tomcat.util.codec.binary.Base64;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.*;

import static java.util.Objects.nonNull;
import static org.springframework.http.HttpStatus.INTERNAL_SERVER_ERROR;
import static org.springframework.http.HttpStatus.NOT_FOUND;

@Slf4j
@Service
@RequiredArgsConstructor
public class DocumentServiceImpl implements DocumentService {

    private final UserService userService;
    private final NotificationService notificationService;
    private final RestTemplate restTemplate;

    @Override
    public User offerCredential(String publicKey, String privateKey, DocumentDraft documentDraft, String patientNumber) {
        User doctor = userService.get(publicKey, privateKey);

        Document document = this.convertToDocument(documentDraft);

        Notification notification = notificationService.addOfferNotification(publicKey, privateKey, doctor,
                document, patientNumber);
        log.info("notification =  '{}'", notification);

        return doctor;
    }

    @Override
    public User accept(String publicKey, String privateKey, Document document, String patientNumber, String doctorNumber) {

        ProcessedUserCredentials doctorCredentials = notificationService.removeOfferNotification(patientNumber, doctorNumber);

        OfferRequest request = OfferRequest.builder().doctorCredentials(doctorCredentials).document(document).build();

        String url = "http://self-sovereign-identity-service/credential/add?public="
                + publicKey + "&private=" + privateKey;
        return restTemplate.postForObject(url, request, User.class);
    }

    @Override
    public void decline(String patientNumber, String doctorNumber) {
        notificationService.removeOfferNotification(patientNumber, doctorNumber);
    }

    @Override
    public User askDocument(String publicKey, String privateKey, List<String> keywords, String patientNumber) {

        User user = userService.get(publicKey, privateKey);

        Notification notification = notificationService.addAskNotification(user, keywords, patientNumber);
        log.info("notification =  '{}'", notification);

        return user;
    }

    @Override
    public User shareDocument(String publicKey, String privateKey, Document document, String doctorNumber) {

        User user = userService.get(publicKey, privateKey);
        String nationalNumber = user.getNationalNumber();

        notificationService.removeAskNotification(nationalNumber, doctorNumber);

        Notification notification = notificationService.addDocumentNotification(user, document, doctorNumber);
        log.info("notification =  '{}'", notification);

        return user;

    }

    @Override
    public void deleteDocument(String publicKey, String privateKey, String credentialId) {
        String url = "http://self-sovereign-identity-service/credential/delete/" + credentialId +
                "?public=" + publicKey + "&private=" + privateKey;
        restTemplate.delete(url);
    }

    private String getTagInformation(AttributeTag attrTag, AttributeList list) {
        return Attribute.getDelimitedStringValuesOrEmptyString(list, attrTag);
    }

    private Document convertToDocument(DocumentDraft documentDraft) {
        if (documentDraft.getType().equals(Document.MedicalDocumentType.Procedure.name())) {
            return this.convertToProcedure((ProcedureDraft) documentDraft);
        } else if (documentDraft.getType().equals(Document.MedicalDocumentType.MedicalImage.name())) {
            return this.convertToMedicalImage((MedicalImageDraft) documentDraft);
        } else throw new ClientException(NOT_FOUND, "Invalid document type has been provided.");
    }

    private Procedure convertToProcedure(ProcedureDraft procedureDraft) {

        String name = procedureDraft.getName();
        Narrative text = new Narrative(Narrative.NarrativeStatus.generated,
                "<div xmlns=\\\"http://www.w3.org/1999/xhtml\\\">" + procedureDraft.getName() + "</div>");
        String status = ProcedureDraft.Status.getValue(procedureDraft.getStatus());

        CodeableConcept notDoneReason = new CodeableConcept();
        String notDoneReasonString = nonNull(procedureDraft.getNotDoneReason()) ? procedureDraft.getNotDoneReason() : "";
        notDoneReason.setText(notDoneReasonString);

        ArrayList<Coding> coding = new ArrayList<>();
        coding.add(new Coding("https://medical-ledger.io/", "1.0", "123456", procedureDraft.getName()));
        CodeableConcept code = new CodeableConcept(coding, procedureDraft.getDescription());

        Reference subject = new Reference(procedureDraft.getPatient().getContactName(), procedureDraft.getPatient().getNationalNumber());

        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
        String startDate = dateFormat.format(new Date(procedureDraft.getStartDate()));

        PerformedPeriod performedPeriod = new PerformedPeriod("", "");
        String performedDateTime = "";
        if (nonNull(procedureDraft.getEndDate())) {
            String endDate = dateFormat.format(new Date(procedureDraft.getEndDate()));
            performedPeriod = new PerformedPeriod(startDate, endDate);
        } else {
            performedDateTime = startDate;
        }

        Reference recorder = new Reference("Doctor", procedureDraft.getRecorder());
        Reference asserter = new Reference("Doctor", procedureDraft.getAsserter());
        Reference performer = new Reference("Doctor", procedureDraft.getPerformer());

        List<CodeableConcept> reasons = new ArrayList<>();
        CodeableConcept reasonCode = new CodeableConcept(Collections.emptyList(), procedureDraft.getReason());
        reasons.add(reasonCode);

        String bodySiteString = nonNull(procedureDraft.getBodySite()) ? procedureDraft.getBodySite() : "";
        CodeableConcept bodySite = new CodeableConcept(Collections.emptyList(), bodySiteString);
        List<CodeableConcept> bodySites = Collections.singletonList(bodySite);

        String complicationString = nonNull(procedureDraft.getComplication()) ? procedureDraft.getComplication() : "";
        CodeableConcept complication = new CodeableConcept(Collections.emptyList(), complicationString);
        List<CodeableConcept> complications = Collections.singletonList(complication);

        String followUpString = Objects.nonNull(procedureDraft.getFollowUp()) ? procedureDraft.getFollowUp() : "";
        CodeableConcept followUp = new CodeableConcept(Collections.emptyList(), followUpString);
        List<CodeableConcept> followUps = Collections.singletonList(followUp);

        String noteString = Objects.nonNull(procedureDraft.getNote()) ? procedureDraft.getNote() : "";
        CodeableConcept note = new CodeableConcept(Collections.emptyList(), noteString);
        List<CodeableConcept> notes = Collections.singletonList(note);

        return new Procedure(name, text, status, notDoneReason, code, subject, performedPeriod, performedDateTime,
                recorder, asserter, performer, reasons, bodySites, complications, followUps, notes);
    }

    private MedicalImage convertToMedicalImage(MedicalImageDraft medicalImageDraft) {

        byte[] imageBytes = Base64.decodeBase64(medicalImageDraft.getImage());

        try (ByteArrayOutputStream baos = new ByteArrayOutputStream()) {

            DicomInputStream dicomInputStream = new DicomInputStream(new ByteArrayInputStream(imageBytes));
            AttributeList list = new AttributeList();
            list.read(dicomInputStream);

            Map<String, String> dicomMap = new HashMap<>();
            List<Field> fields = Arrays.asList(TagFromName.class.getDeclaredFields());
            fields.stream().filter(field -> field.getType() == AttributeTag.class).forEach(field -> {

                if (Modifier.isFinal(field.getModifiers())) {
                    try {
                        String tagInformation = this.getTagInformation((AttributeTag) field.get(null), list);
                        if (!tagInformation.isEmpty())
                            dicomMap.put(field.getName(), tagInformation);
                    } catch (IllegalAccessException e) {
                        throw new ServerException(INTERNAL_SERVER_ERROR, e.getMessage());
                    }
                }
            });

            BufferedImage bufferedImage = ConsumerFormatImageMaker.makeEightBitImage(list);
            ImageIO.write(bufferedImage, "png", baos);
            baos.flush();
            byte[] imageInByte = baos.toByteArray();
            String encodedImage = new String(Base64.encodeBase64(imageInByte), StandardCharsets.UTF_8);
            System.out.println("encoded image: " + encodedImage);

            dicomMap.put("Image", encodedImage);
            System.out.println("dicomMap: " + dicomMap);

            return new MedicalImage(dicomMap);
        } catch (Exception e) {
            log.error("Exception was thrown: " + e);
            throw new ServerException(INTERNAL_SERVER_ERROR, e.getMessage());
        }
    }
}
