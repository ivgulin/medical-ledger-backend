package com.mokujin.ssi.model.internal;


import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Accessors(chain = true)
@Builder(toBuilder = true)
public class Contact {

    private String contactName;

    private String photo;

    @JsonIgnore
    private boolean isVerinym;

}
