package com.mokujin.ssi.model.document.medical.hl7;

import com.mokujin.ssi.model.document.Document;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class ModifiedProcedure extends Document {

    private String id;
    private String textStatus;
    private String textDiv;
    private String status;
    private String notDoneReason;
    private String codeSystem;
    private String codeVersion;
    private String code;
    private String codeDisplay;
    private String subjectReference;
    private String subjectDisplay;
    private String performedDateTime;
    private String start;
    private String end;
    private String recorderReference;
    private String recorderDisplay;
    private String asserterReference;
    private String asserterDisplay;
    private String performerReference;
    private String performerDisplay;
    private String reasonCode;
    private String bodySite;
    private String complication;
    private String followUp;
    private String note;

    public ModifiedProcedure(Procedure procedure) {
        super(MedicalDocumentType.Procedure.name());
        this.id = procedure.getId();
        this.textStatus = procedure.getText().getStatus().name();
        this.textDiv = procedure.getText().getDiv();
        this.status = procedure.getStatus();
        this.notDoneReason = procedure.getNotDoneReason().getText();
        this.codeSystem = procedure.getCode().getCoding().get(0).getSystem();
        this.codeVersion = procedure.getCode().getCoding().get(0).getVersion();
        this.code = procedure.getCode().getCoding().get(0).getCode();
        this.codeDisplay = procedure.getCode().getCoding().get(0).getDisplay();
        this.subjectReference = procedure.getSubject().getReference();
        this.subjectDisplay = procedure.getSubject().getDisplay();
        this.performedDateTime = procedure.getPerformedDateTime();
        this.start = procedure.getPerformedPeriod().getStart();
        this.end = procedure.getPerformedPeriod().getEnd();
        this.recorderReference = procedure.getRecorder().getReference();
        this.recorderDisplay = procedure.getRecorder().getDisplay();
        this.asserterReference = procedure.getAsserter().getReference();
        this.asserterDisplay = procedure.getAsserter().getDisplay();
        this.performerReference = procedure.getPerformer().getReference();
        this.performerDisplay = procedure.getPerformer().getDisplay();
        this.reasonCode = procedure.getReasonCode().get(0).getText();
        this.bodySite = procedure.getBodySite().get(0).getText();
        this.complication = procedure.getComplication().get(0).getText();
        this.followUp = procedure.getFollowUp().get(0).getText();
        this.note = procedure.getNote().get(0).getText();
    }

    public ModifiedProcedure() {
        super(MedicalDocumentType.Procedure.name());
    }

}
