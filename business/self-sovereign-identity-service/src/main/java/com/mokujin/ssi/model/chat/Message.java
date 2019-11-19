package com.mokujin.ssi.model.chat;

import lombok.Data;

@Data
public class Message {

    private Long date;

    private String content;

    private boolean isMine;

}
