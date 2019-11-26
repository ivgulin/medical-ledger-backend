package com.mokujin.ssi.model.user.request;

import com.mokujin.ssi.model.document.Document;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class OfferRequest {

    private UserCredentials doctorCredentials;

    private Document document;

}
