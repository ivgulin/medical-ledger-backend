package com.mokujin.user.service.impl;

import com.mokujin.user.model.User;
import com.mokujin.user.model.document.Document;
import com.mokujin.user.model.document.impl.medical.dicom.MedicalImage;
import com.mokujin.user.model.document.impl.medical.hl7.Procedure;
import com.mokujin.user.model.document.impl.medical.hl7.component.*;
import com.mokujin.user.model.exception.extention.ClientException;
import com.mokujin.user.model.exception.extention.ServerException;
import com.mokujin.user.model.internal.DocumentDraft;
import com.mokujin.user.model.internal.ProcedureDraft;
import com.mokujin.user.model.notification.Notification;
import com.mokujin.user.service.DocumentService;
import com.mokujin.user.service.NotificationService;
import com.mokujin.user.service.UserService;
import com.pixelmed.dicom.*;
import com.pixelmed.display.ConsumerFormatImageMaker;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FileUtils;
import org.apache.tomcat.util.codec.binary.Base64;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.multipart.MultipartFile;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.*;

import static org.springframework.http.HttpStatus.INTERNAL_SERVER_ERROR;
import static org.springframework.http.HttpStatus.NOT_FOUND;

@Slf4j
@Service
@RequiredArgsConstructor
public class DocumentServiceImpl implements DocumentService {

    private final UserService userService;
    private final NotificationService notificationService;
    private final RestTemplate restTemplate;

    @Value(value = "${dicom.path}")
    private String filePath;

    @Override
    public User offerDicom(String publicKey, String privateKey, MultipartFile document, String patientNumber) {

        User user = userService.get(publicKey, privateKey);

        try (ByteArrayOutputStream baos = new ByteArrayOutputStream()) {

            DicomInputStream dicomInputStream = new DicomInputStream(document.getInputStream());
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

            Notification notification = notificationService.addOfferNotification(user, new MedicalImage(dicomMap), patientNumber);
            log.info("notification =  '{}'", notification);

            return user;
        } catch (Exception e) {
            log.error("Exception was thrown: " + e);
            throw new ServerException(INTERNAL_SERVER_ERROR, e.getMessage());
        }
    }

    @Override
    public User offerCredential(String publicKey, String privateKey, DocumentDraft documentDraft, String patientNumber) {
        User user = userService.get(publicKey, privateKey);

        Document document = this.convertToDocument(documentDraft);

        Notification notification = notificationService.addOfferNotification(user, document, patientNumber);
        log.info("notification =  '{}'", notification);

        return user;
    }

    private String getTagInformation(AttributeTag attrTag, AttributeList list) {
        return Attribute.getDelimitedStringValuesOrEmptyString(list, attrTag);
    }

    private Document convertToDocument(DocumentDraft documentDraft) {
        if (documentDraft.getType().equals(Document.MedicalDocumentType.Procedure.name())) {
            ProcedureDraft draft = (ProcedureDraft) documentDraft;

            String name = draft.getName();
            Narrative text = new Narrative(Narrative.NarrativeStatus.generated,
                    "<div xmlns=\\\"http://www.w3.org/1999/xhtml\\\">" + draft.getName() + "</div>");
            String status = ProcedureDraft.Status.getValue(draft.getStatus());

            CodeableConcept notDoneReason = new CodeableConcept();
            notDoneReason.setText(draft.getNotDoneReason());

            ArrayList<Coding> coding = new ArrayList<>();
            coding.add(new Coding("https://medical-ledger.io/", "1.0", "123456", draft.getName()));
            CodeableConcept code = new CodeableConcept(coding, draft.getDescription());

            Reference subject = new Reference(draft.getPatient().getContactName(), draft.getPatient().getNationalNumber());

            SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
            String startDate = dateFormat.format(new Date(draft.getStartDate()));

            PerformedPeriod performedPeriod = null;
            String performedDateTime = null;
            if (Objects.nonNull(draft.getEndDate())) {
                String endDate = dateFormat.format(new Date(draft.getEndDate()));
                performedPeriod = new PerformedPeriod(startDate, endDate);
            } else {
                performedDateTime = startDate;
            }

            Reference recorder = new Reference("Doctor", draft.getRecorder());
            Reference asserter = new Reference("Doctor", draft.getAsserter());
            Reference performer = new Reference("Doctor", draft.getPerformer());

            List<CodeableConcept> reasons = new ArrayList<>();
            CodeableConcept reasonCode = new CodeableConcept(Collections.emptyList(), draft.getReason());
            reasons.add(reasonCode);

            List<CodeableConcept> bodySites = new ArrayList<>();
            CodeableConcept bodySite = new CodeableConcept(Collections.emptyList(), draft.getBodySite());
            bodySites.add(bodySite);

            List<CodeableConcept> complications = new ArrayList<>();
            CodeableConcept complication = new CodeableConcept(Collections.emptyList(), draft.getComplication());
            complications.add(complication);

            List<CodeableConcept> followUps = new ArrayList<>();
            CodeableConcept followUp = new CodeableConcept(Collections.emptyList(), draft.getFollowUp());
            followUps.add(followUp);

            List<CodeableConcept> notes = new ArrayList<>();
            CodeableConcept note = new CodeableConcept(Collections.emptyList(), draft.getNote());
            notes.add(note);

            return new Procedure(name, text, status, notDoneReason, code, subject, performedPeriod, performedDateTime,
                    recorder, asserter, performer, reasons, bodySites, complications, followUps, notes);
        } else throw new ClientException(NOT_FOUND, "Invalid document type has been provided.");
    }
}
