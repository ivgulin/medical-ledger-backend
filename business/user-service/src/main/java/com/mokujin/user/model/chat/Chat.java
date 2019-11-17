package com.mokujin.user.model.chat;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
public class Chat {

    private String notificationToken;

    private List<Message> messages = new ArrayList<>();

}
