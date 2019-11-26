package com.mokujin.user.model.internal;

import com.mokujin.user.model.ProcessedUserCredentials;
import com.mokujin.user.model.document.Document;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class OfferRequest {

    private ProcessedUserCredentials doctorCredentials;

    private Document document;

}
