package com.mokujin.ssi.model.document.medical.hl7;

import com.mokujin.ssi.model.document.Document;
import com.mokujin.ssi.model.document.medical.hl7.component.*;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.List;

import static com.mokujin.ssi.model.document.medical.hl7.component.Narrative.NarrativeStatus.valueOf;
import static java.util.Collections.singletonList;

@Data
@EqualsAndHashCode(callSuper = true)
public class Procedure extends Document {

    private String id;

    private Narrative text;

    private String status;

    private CodeableConcept notDoneReason;

    private CodeableConcept code;

    private Reference subject;

    private PerformedPeriod performedPeriod;

    private String performedDateTime;

    private Reference recorder;

    private Reference asserter;

    private Reference performer;

    private List<CodeableConcept> reasonCode;

    private List<CodeableConcept> bodySite;

    private List<CodeableConcept> complication;

    private List<CodeableConcept> followUp;

    private List<CodeableConcept> note;

    public Procedure(String id, Narrative text, String status, CodeableConcept notDoneReason,
                     CodeableConcept code, Reference subject, PerformedPeriod performedPeriod, String performedDateTime,
                     Reference recorder, Reference asserter, Reference performer, List<CodeableConcept> reasonCode,
                     List<CodeableConcept> bodySite, List<CodeableConcept> complication, List<CodeableConcept> followUp,
                     List<CodeableConcept> note) {
        super(MedicalDocumentType.Procedure.name());
        this.id = id;
        this.text = text;
        this.status = status;
        this.notDoneReason = notDoneReason;
        this.code = code;
        this.subject = subject;
        this.performedPeriod = performedPeriod;
        this.performedDateTime = performedDateTime;
        this.recorder = recorder;
        this.asserter = asserter;
        this.performer = performer;
        this.reasonCode = reasonCode;
        this.bodySite = bodySite;
        this.complication = complication;
        this.followUp = followUp;
        this.note = note;
    }

    public Procedure() {
        super(MedicalDocumentType.Procedure.name());
    }

    public Procedure(LedgerModifiedProcedure modifiedProcedure) {
        super(MedicalDocumentType.Procedure.name());
        this.id = modifiedProcedure.getId();
        this.text = new Narrative(valueOf(modifiedProcedure.getTextStatus()), modifiedProcedure.getTextDiv());
        this.status = modifiedProcedure.getStatus();
        this.notDoneReason = new CodeableConcept(null, modifiedProcedure.getNotDoneReason());
        this.code = new CodeableConcept(singletonList(new Coding(modifiedProcedure.getCodeSystem(),
                modifiedProcedure.getCodeVersion(), modifiedProcedure.getCode(), modifiedProcedure.getCodeDisplay())),
                modifiedProcedure.getCodeText());
        this.subject = new Reference(modifiedProcedure.getSubjectReference(), modifiedProcedure.getSubjectDisplay());
        this.performedPeriod = new PerformedPeriod(modifiedProcedure.getStart(), modifiedProcedure.getEnd());
        this.performedDateTime = modifiedProcedure.getPerformedDateTime();
        this.recorder = new Reference(modifiedProcedure.getRecorderReference(), modifiedProcedure.getRecorderDisplay());
        this.asserter = new Reference(modifiedProcedure.getAsserterReference(), modifiedProcedure.getAsserterDisplay());
        this.performer = new Reference(modifiedProcedure.getPerformerReference(), modifiedProcedure.getPerformerDisplay());
        this.reasonCode = singletonList(new CodeableConcept(null, modifiedProcedure.getReasonCode()));
        this.bodySite = singletonList(new CodeableConcept(null, modifiedProcedure.getBodySite()));
        this.complication = singletonList(new CodeableConcept(null, modifiedProcedure.getComplication()));
        this.followUp = singletonList(new CodeableConcept(null, modifiedProcedure.getFollowUp()));
        this.note = singletonList(new CodeableConcept(null, modifiedProcedure.getNote()));
    }
}
