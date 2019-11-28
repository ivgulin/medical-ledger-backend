package com.mokujin.user.model.document.impl.medical.hl7.component;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class PerformedPeriod implements Serializable {

    private String start;

    private String end;

}
