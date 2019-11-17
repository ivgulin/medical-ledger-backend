package com.mokujin.ssi.service.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.mokujin.ssi.model.chat.Chat;
import com.mokujin.ssi.model.chat.LedgerChatResponse;
import com.mokujin.ssi.model.chat.Message;
import com.mokujin.ssi.service.ChatService;
import com.mokujin.ssi.service.WalletService;
import lombok.AllArgsConstructor;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.hyperledger.indy.sdk.non_secrets.WalletRecord;
import org.hyperledger.indy.sdk.wallet.Wallet;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class ChatServiceImpl implements ChatService {

    private final ObjectMapper objectMapper;
    private final WalletService walletService;

    @Override
    @SneakyThrows
    public Chat get(String publicKey, String privateKey, String connectionNumber, String notificationToken) {

        Wallet wallet = walletService.getOrCreateWallet(publicKey, privateKey);

        Chat chat;
        try {
            String chatInString = WalletRecord.get(wallet, "chat", connectionNumber, "{}").get()
                    .replace("\\", "")
                    .replace("\"{", "{")
                    .replace("}\"", "}");;
            log.info("'chatInString={}'", chatInString);
            chat = objectMapper.readValue(chatInString, LedgerChatResponse.class).getValue();
        } catch (Exception e) {
            chat = new Chat();
            chat.setNotificationToken(notificationToken);
            String chatInString = objectMapper.writeValueAsString(chat);
            WalletRecord.add(wallet, "chat", connectionNumber, chatInString, "{}");
        }

        wallet.close();
        return chat;
    }



    @Override
    @SneakyThrows
    public Chat addMessage(String publicKey, String privateKey, String connectionNumber,
                           Message message, String notificationToken) {

        Chat chat = this.get(publicKey, privateKey, connectionNumber, notificationToken);
        chat.addMessage(message);
        String chatInString = objectMapper.writeValueAsString(chat);

        Wallet wallet = walletService.getOrCreateWallet(publicKey, privateKey);
        WalletRecord.updateValue(wallet, "chat", connectionNumber, chatInString);
        wallet.close();

        return chat;
    }
}
