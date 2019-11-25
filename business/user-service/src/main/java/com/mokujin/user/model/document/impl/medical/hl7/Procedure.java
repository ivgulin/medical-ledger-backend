package com.mokujin.user.model.document.impl.medical.hl7;

import com.mokujin.user.model.document.Document;
import com.mokujin.user.model.document.impl.medical.hl7.component.CodeableConcept;
import com.mokujin.user.model.document.impl.medical.hl7.component.Narrative;
import com.mokujin.user.model.document.impl.medical.hl7.component.PerformedPeriod;
import com.mokujin.user.model.document.impl.medical.hl7.component.Reference;
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

    private Reference accepter;

    private Reference performer;

    private List<CodeableConcept> reasonCode;

    private List<CodeableConcept> bodySite;

    private List<CodeableConcept> complication;

    private List<CodeableConcept> followUp;

    private List<CodeableConcept> note;


    public Procedure() {
        super(MedicalDocumentType.Procedure.name());
    }


    public enum Status {
        preparation,
        progress,
        suspended,
        aborted,
        completed,
        error,
        unknown;

        public String getValue(Status status) {
            if (status.equals(progress))
                return "in-progress";
            if (status.equals(error))
                return "entered-in-error";
            return status.name();
        }
    }

}
