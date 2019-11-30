package com.mokujin.ssi.service.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.mokujin.ssi.model.chat.Chat;
import com.mokujin.ssi.model.chat.LedgerChatResponse;
import com.mokujin.ssi.model.chat.Message;
import com.mokujin.ssi.model.exception.BusinessException;
import com.mokujin.ssi.model.exception.extention.LedgerException;
import com.mokujin.ssi.service.ChatService;
import com.mokujin.ssi.service.WalletService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.hyperledger.indy.sdk.non_secrets.WalletRecord;
import org.hyperledger.indy.sdk.wallet.Wallet;
import org.springframework.stereotype.Service;

import static org.springframework.http.HttpStatus.INTERNAL_SERVER_ERROR;

@Slf4j
@Service
@RequiredArgsConstructor
public class ChatServiceImpl implements ChatService {

    private final ObjectMapper objectMapper;
    private final WalletService walletService;

    @Override
    public Chat get(String publicKey, String privateKey, String connectionNumber) {

        try (Wallet wallet = walletService.getOrCreateWallet(publicKey, privateKey);) {
            return this.getOrCreateChat(connectionNumber, wallet);
        } catch (BusinessException e) {
            log.error("Exception was thrown: " + e);
            throw new LedgerException(e.getStatusCode(), e.getMessage());
        } catch (Exception e) {
            log.error("Exception was thrown: " + e);
            throw new LedgerException(INTERNAL_SERVER_ERROR, e.getMessage());
        }
    }

    Chat getOrCreateChat(String connectionNumber, Wallet wallet) throws Exception {
        Chat chat;
        try {
            String chatInString = WalletRecord.get(wallet, "chat", connectionNumber, "{}").get()
                    .replace("\\", "")
                    .replace("\"{", "{")
                    .replace("}\"", "}");

            log.info("'chatInString={}'", chatInString);
            chat = objectMapper.readValue(chatInString, LedgerChatResponse.class).getValue();
            return chat;
        } catch (Exception e) {
            chat = new Chat();
            String chatInString = objectMapper.writeValueAsString(chat);
            WalletRecord.add(wallet, "chat", connectionNumber, chatInString, "{}");
            return chat;
        }
    }

    @Override
    public Chat addMessage(String publicKey, String privateKey, String connectionNumber, Message message) {

        try (Wallet wallet = walletService.getOrCreateWallet(publicKey, privateKey)) {
            Chat chat = this.getOrCreateChat(connectionNumber, wallet);
            chat.addMessage(message);
            String chatInString = objectMapper.writeValueAsString(chat);

            WalletRecord.updateValue(wallet, "chat", connectionNumber, chatInString);
            return chat;
        }catch (BusinessException e) {
            log.error("Exception was thrown: " + e);
            throw new LedgerException(e.getStatusCode(), e.getMessage());
        } catch (Exception e) {
            log.error("Exception was thrown: " + e);
            throw new LedgerException(INTERNAL_SERVER_ERROR, e.getMessage());
        }
    }
}
