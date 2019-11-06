package com.mokujin.ssi.model;


import lombok.*;
import lombok.experimental.Accessors;
import org.hyperledger.indy.sdk.wallet.Wallet;

import java.util.ArrayList;
import java.util.List;

import static lombok.AccessLevel.PRIVATE;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Accessors(chain = true)
@Builder(toBuilder = true)
public class Identity {

    private String verinymDid;

    private Wallet wallet;

    private List<Pseudonym> pseudonyms = new ArrayList<>();

    public void addPseudonym(Pseudonym pseudonym) {
        pseudonyms.add(pseudonym);
    }

}
