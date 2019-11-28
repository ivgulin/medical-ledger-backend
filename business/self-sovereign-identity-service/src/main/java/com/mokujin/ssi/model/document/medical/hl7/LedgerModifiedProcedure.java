package com.mokujin.ssi.model.document.medical.hl7;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class LedgerModifiedProcedure {

    private String resourceType;
    private String id;
    private String textStatus;
    private String textDiv;
    private String status;
    private String notDoneReason;
    private String codeSystem;
    private String codeVersion;
    private String code;
    private String codeDisplay;
    private String codeText;
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

}
