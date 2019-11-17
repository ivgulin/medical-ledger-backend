package com.mokujin.ssi.model.chat;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
public class Chat {

    private List<Message> messages = new ArrayList<>();

    public void addMessage(Message message) {
        messages.add(message);
    }

}
