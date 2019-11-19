package com.mokujin.user.model;


import lombok.Builder;
import lombok.Data;

import java.io.Serializable;

@Data
@Builder
public class Contact implements Serializable {

    private String contactName;

    private String photo;

    private String nationalNumber;

    private boolean isVisible;

}
