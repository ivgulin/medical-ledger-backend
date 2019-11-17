package com.mokujin.user.model.chat;

import com.mokujin.user.model.Contact;
import lombok.Data;

import javax.validation.constraints.NotNull;

@Data
public class Message {

    private Contact contact;

    @NotNull(message = "Message date is required")
    private Long date;

    @NotNull(message = "Message content is required")
    private String content;

    @NotNull(message = "Message affiliation is required")
    private boolean isMine;

}
