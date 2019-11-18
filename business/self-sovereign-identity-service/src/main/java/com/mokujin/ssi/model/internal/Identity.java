package com.mokujin.ssi.model.internal;


import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;
import org.hyperledger.indy.sdk.wallet.Wallet;

import java.util.ArrayList;
import java.util.List;

import static com.mokujin.ssi.model.internal.Role.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Accessors(chain = true)
@Builder(toBuilder = true)
public class Identity {

    private Role role = PATIENT;

    private String verinymDid;

    private Wallet wallet;

    private List<Pseudonym> pseudonyms = new ArrayList<>();

    private List<Credential> credentials = new ArrayList<>();

    public void addPseudonym(Pseudonym pseudonym) {
        pseudonyms.add(pseudonym);
    }

}
