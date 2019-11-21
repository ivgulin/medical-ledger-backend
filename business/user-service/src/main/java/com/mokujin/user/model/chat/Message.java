package com.mokujin.user.model.chat;

import com.mokujin.user.model.Contact;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotNull;
import java.io.Serializable;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class Message implements Serializable {

    @NotNull(message = "Contact is required")
    private Contact contact;

    @NotNull(message = "Message date is required")
    private Long date;

    @NotNull(message = "Message content is required")
    private String content;

    @NotNull(message = "Message affiliation is required")
    private boolean isMine;

}
