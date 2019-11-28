package com.mokujin.user.model.presentation;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
public class PresentationRequest {

    private List<String> passportAttributes = new ArrayList<>();
    private List<String> nationalNumberAttributes = new ArrayList<>();
    private List<String> diplomaAttributes = new ArrayList<>();
    private List<String> certificateAttributes = new ArrayList<>();
}
