package com.mokujin.ssi.service.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.mokujin.ssi.model.chat.Chat;
import com.mokujin.ssi.model.chat.LedgerChatResponse;
import com.mokujin.ssi.model.chat.Message;
import com.mokujin.ssi.model.exception.extention.LedgerException;
import com.mokujin.ssi.service.WalletService;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import mockit.MockUp;
import org.hyperledger.indy.sdk.non_secrets.WalletRecord;
import org.hyperledger.indy.sdk.wallet.Wallet;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.concurrent.CompletableFuture;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;
import static org.springframework.http.HttpStatus.INTERNAL_SERVER_ERROR;

@Slf4j
@ExtendWith(MockitoExtension.class)
class ChatServiceImplTest {

    private ObjectMapper objectMapper = new ObjectMapper();

    @Mock
    private WalletService walletService;

    private ChatServiceImpl chatService;

    @BeforeEach
    void setUp() {
        chatService = new ChatServiceImpl(objectMapper, walletService);
    }

    @Test
    @SneakyThrows
    void get_exceptionOccursInsideTryBlock_walletIsClosedAndExceptionIsThrown() {

        String publicKey = "public";
        String privateKey = "private";
        String connectionNumber = "number";

        Wallet wallet = Mockito.mock(Wallet.class);
        when(walletService.getOrCreateWallet(publicKey, privateKey)).thenReturn(wallet);

        chatService = spy(chatService);
        doThrow(new LedgerException(INTERNAL_SERVER_ERROR, "Test exception"))
                .when(chatService).getOrCreateChat(connectionNumber, wallet);

        assertThrows(LedgerException.class, () -> chatService.get(publicKey, privateKey, connectionNumber));
        verify(wallet, times(1)).close();
    }

    @Test
    @SneakyThrows
    void get_validInputs_chatIsReturned() {

        String publicKey = "public";
        String privateKey = "private";
        String connectionNumber = "number";

        Wallet wallet = Mockito.mock(Wallet.class);
        when(walletService.getOrCreateWallet(publicKey, privateKey)).thenReturn(wallet);

        chatService = spy(chatService);
        Chat chat = new Chat();
        doReturn(chat).when(chatService).getOrCreateChat(connectionNumber, wallet);

        Chat result = chatService.get(publicKey, privateKey, connectionNumber);

        assertEquals(chat, result);
        verify(wallet, times(1)).close();
    }

    @Test
    @SneakyThrows
    void getOrCreateChat_noRecordsAvailable_newChatIsReturned() {

        String connectionNumber = "number";

        Wallet userWallet = Mockito.mock(Wallet.class);

        Chat chat = new Chat();
        String chatInString = objectMapper.writeValueAsString(chat);

        new MockUp<WalletRecord>() {
            @mockit.Mock
            public CompletableFuture<String> get(Wallet wallet, String type, String id, String optionsJson) {
                assertEquals(userWallet, wallet);
                assertEquals("chat", type);
                assertEquals(connectionNumber, id);

                CompletableFuture<String> future = new CompletableFuture<>();
                future.complete("");
                return future;
            }

            @mockit.Mock
            public CompletableFuture<Void> add(Wallet wallet, String type, String id, String value, String tagsJson) {
                assertEquals(userWallet, wallet);
                assertEquals("chat", type);
                assertEquals(connectionNumber, id);
                assertEquals(chatInString, value);

                return CompletableFuture.runAsync(() -> log.debug("In mock."));
            }
        };

        Chat result = chatService.getOrCreateChat(connectionNumber, userWallet);
        assertEquals(chat, result);
    }

    @Test
    @SneakyThrows
    void getOrCreateChat_recordsAvailable_chatIsReturned() {

        String connectionNumber = "number";

        Wallet userWallet = Mockito.mock(Wallet.class);

        LedgerChatResponse response = new LedgerChatResponse();
        Chat chat = new Chat();
        response.setValue(chat);
        String chatInString = objectMapper.writeValueAsString(response);

        new MockUp<WalletRecord>() {
            @mockit.Mock
            public CompletableFuture<String> get(Wallet wallet, String type, String id, String optionsJson) {
                assertEquals(userWallet, wallet);
                assertEquals("chat", type);
                assertEquals(connectionNumber, id);

                CompletableFuture<String> future = new CompletableFuture<>();
                future.complete(objectMapper.createObjectNode().put("test", chatInString).get("test").asText());
                return future;
            }
        };

        Chat result = chatService.getOrCreateChat(connectionNumber, userWallet);
        assertEquals(chat, result);
    }

    @Test
    @SneakyThrows
    void addMessage_exceptionOccursInsideTryBlock_walletIsClosedAndExceptionIsThrown() {

        String publicKey = "public";
        String privateKey = "private";
        String connectionNumber = "number";
        Message message = new Message();

        Wallet wallet = Mockito.mock(Wallet.class);
        when(walletService.getOrCreateWallet(publicKey, privateKey)).thenReturn(wallet);

        chatService = spy(chatService);
        doThrow(new LedgerException(INTERNAL_SERVER_ERROR, "Test exception"))
                .when(chatService).getOrCreateChat(connectionNumber, wallet);

        assertThrows(LedgerException.class, () -> chatService.addMessage(publicKey, privateKey, connectionNumber, message));
        verify(wallet, times(1)).close();
    }

    @Test
    @SneakyThrows
    void addMessage_validInputs_chatIsReturned() {

        String publicKey = "public";
        String privateKey = "private";
        String connectionNumber = "number";
        Message message = new Message();

        Wallet userWallet = Mockito.mock(Wallet.class);
        when(walletService.getOrCreateWallet(publicKey, privateKey)).thenReturn(userWallet);

        chatService = spy(chatService);
        Chat chat = new Chat();
        doReturn(chat).when(chatService).getOrCreateChat(connectionNumber, userWallet);
        Chat expected = new Chat();
        expected.addMessage(message);
        String chatInString = objectMapper.writeValueAsString(expected);

        new MockUp<WalletRecord>() {
            @mockit.Mock
            public CompletableFuture<Void> updateValue(Wallet wallet, String type, String id, String value) {
                assertEquals(userWallet, wallet);
                assertEquals("chat", type);
                assertEquals(connectionNumber, id);
                assertEquals(chatInString, value);

                return CompletableFuture.runAsync(() -> log.debug("In mock."));
            }
        };

        Chat result = chatService.addMessage(publicKey, privateKey, connectionNumber, message);

        assertEquals(chat, result);
        verify(userWallet, times(1)).close();
    }
}