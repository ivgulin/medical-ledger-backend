package com.mokujin.user.model.presentation;

import lombok.Data;

import java.util.List;

@Data
public class PresentationRequest {

    private List<String> passportAttributes;
    private List<String> nationalNumberAttributes;
    private List<String> diplomaAttributes;
    private List<String> certificateAttributes;
}
