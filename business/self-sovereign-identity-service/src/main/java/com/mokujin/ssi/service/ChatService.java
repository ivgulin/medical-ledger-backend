package com.mokujin.ssi.service;

import com.mokujin.ssi.model.chat.Chat;
import com.mokujin.ssi.model.chat.Message;

public interface ChatService {

    Chat get(String publicKey, String privateKey, String connectionNumber);

    Chat addMessage(String publicKey, String privateKey, String connectionNumber, Message message);
}
