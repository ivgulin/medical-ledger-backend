package com.mokujin.user.model.internal;

import com.mokujin.user.model.Contact;
import lombok.Data;
import lombok.EqualsAndHashCode;

import javax.validation.constraints.NotNull;

@Data
@EqualsAndHashCode(callSuper = true)
public class ProcedureDraft extends DocumentDraft {

    @NotNull
    private String name;

    @NotNull
    private String description;

    @NotNull
    private Status status;

    private String notDoneReason;

    @NotNull
    private Contact patient;

    @NotNull
    private Long startDate;

    private Long endDate;

    @NotNull
    private String recorder;

    @NotNull
    private String asserter;

    @NotNull
    private String performer;

    @NotNull
    private String reason;

    private String bodySite;

    private String complication;

    private String followUp;

    private String note;

    public ProcedureDraft() {
        super(Type.Procedure.name());
    }

    public enum Status {
        planned,
        active,
        abandoned,
        failed,
        completed,
        error,
        unknown;

        public static String getValue(Status status) {
            if (status.equals(planned))
                return "preparation";
            if (status.equals(active))
                return "in-progress";
            if (status.equals(abandoned))
                return "not-done";
            if (status.equals(failed))
                return "stopped";
            if (status.equals(error))
                return "entered-in-error";

            return status.name();
        }
    }
}
