package com.mokujin.user.model.presentation;

import com.mokujin.user.model.Contact;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class Affirmation {

    private boolean result;

    private Contact issuedBy;

}
