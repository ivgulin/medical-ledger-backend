package com.mokujin.user.model;


import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class Contact implements Serializable {

    private String contactName;

    private String photo;

    private String nationalNumber;

    private boolean isVisible;

}
