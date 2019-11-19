package com.mokujin.user.model.chat;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
public class Chat {

    private List<Message> messages = new ArrayList<>();

}
