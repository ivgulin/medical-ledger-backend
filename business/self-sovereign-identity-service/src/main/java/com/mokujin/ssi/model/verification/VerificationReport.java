package com.mokujin.ssi.model.verification;

import com.mokujin.ssi.model.internal.Contact;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class VerificationReport {

    private boolean result;

    private Contact issuedBy;

}
