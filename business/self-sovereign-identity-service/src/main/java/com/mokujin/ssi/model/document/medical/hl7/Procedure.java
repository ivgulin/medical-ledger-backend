package com.mokujin.ssi.model.document.medical.hl7;

import com.mokujin.ssi.model.document.Document;
import com.mokujin.ssi.model.document.medical.hl7.component.CodeableConcept;
import com.mokujin.ssi.model.document.medical.hl7.component.Narrative;
import com.mokujin.ssi.model.document.medical.hl7.component.PerformedPeriod;
import com.mokujin.ssi.model.document.medical.hl7.component.Reference;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.List;

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

}
