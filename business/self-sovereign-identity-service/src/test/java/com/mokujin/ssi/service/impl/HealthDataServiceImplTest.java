package com.mokujin.ssi.service.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.mokujin.ssi.model.chat.Chat;
import com.mokujin.ssi.model.exception.extention.LedgerException;
import com.mokujin.ssi.model.record.HealthRecord;
import com.mokujin.ssi.model.record.LedgerHealthData;
import com.mokujin.ssi.model.record.impl.HeartHealthRecord;
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

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;
import static org.springframework.http.HttpStatus.INTERNAL_SERVER_ERROR;

@Slf4j
@ExtendWith(MockitoExtension.class)
class HealthDataServiceImplTest {

    private ObjectMapper objectMapper = new ObjectMapper();

    @Mock
    private WalletService walletService;

    private HealthDataServiceImpl healthDataService;

    @BeforeEach
    void setUp() {
        healthDataService = new HealthDataServiceImpl(walletService, objectMapper);
    }


    @Test
    @SneakyThrows
    void save_validInputs_listOfRecordsAreReturned() {

        String publicKey = "public";
        String privateKey = "private";

        HeartHealthRecord record = new HeartHealthRecord();
        record.setOxygenSaturation(90);

        Wallet userWallet = Mockito.mock(Wallet.class);
        when(walletService.getOrCreateWallet(publicKey, privateKey)).thenReturn(userWallet);

        healthDataService = spy(healthDataService);
        doReturn(new ArrayList<>()).when(healthDataService).getRecords(userWallet);

        List<HealthRecord> expected = new ArrayList<>();
        expected.add(record);
        String listOnString = objectMapper.writeValueAsString(expected);

        new MockUp<WalletRecord>() {
            @mockit.Mock
            public CompletableFuture<Void> updateValue(Wallet wallet, String type, String id, String value) {
                assertEquals(userWallet, wallet);
                assertEquals("health", type);
                assertEquals("records", id);
                assertEquals(listOnString, value);

                return CompletableFuture.runAsync(() -> log.debug("In mock."));
            }
        };

        List<HealthRecord> result = healthDataService.save(publicKey, privateKey, record);

        assertEquals(expected, result);
        verify(userWallet, times(1)).close();
    }


    @Test
    @SneakyThrows
    void save_exceptionOccursInsideTryBlock_walletIsClosedAndExceptionIsThrown() {

        String publicKey = "public";
        String privateKey = "private";

        HeartHealthRecord record = new HeartHealthRecord();
        record.setOxygenSaturation(90);

        Wallet wallet = Mockito.mock(Wallet.class);
        when(walletService.getOrCreateWallet(publicKey, privateKey)).thenReturn(wallet);

        healthDataService = spy(healthDataService);

        doThrow(new Exception("test")).when(healthDataService).getRecords(wallet);

        assertThrows(LedgerException.class, () -> healthDataService.save(publicKey, privateKey, record));
        verify(wallet, times(1)).close();
    }

    @Test
    @SneakyThrows
    void getRecords_noRecordsAreAvailable_listOfRecordsAreReturned() {

        Wallet userWallet = Mockito.mock(Wallet.class);

        List<HealthRecord> records = new ArrayList<>();
        String listOnString = objectMapper.writeValueAsString(records);

        new MockUp<WalletRecord>() {
            @mockit.Mock
            public CompletableFuture<String> get(Wallet wallet, String type, String id, String optionsJson) {
                assertEquals(userWallet, wallet);
                assertEquals("health", type);
                assertEquals("records", id);

                CompletableFuture<String> future = new CompletableFuture<>();
                future.complete("");
                return future;
            }

            @mockit.Mock
            public CompletableFuture<Void> add(Wallet wallet, String type, String id, String value, String tagsJson) {
                assertEquals(userWallet, wallet);
                assertEquals("health", type);
                assertEquals("records", id);
                assertEquals(listOnString, value);

                return CompletableFuture.runAsync(() -> log.debug("In mock."));
            }
        };

        List<HealthRecord> result = healthDataService.getRecords(userWallet);

        assertEquals(records, result);
    }


    @Test
    @SneakyThrows
    void getRecords_recordsAreAvailable_listOfRecordsAreReturned() {

        Wallet userWallet = Mockito.mock(Wallet.class);

        LedgerHealthData ledgerHealthData = new LedgerHealthData();
        List<HealthRecord> records = new ArrayList<>();
        ledgerHealthData.setValue(records);
        String listOnString = objectMapper.writeValueAsString(ledgerHealthData);

        new MockUp<WalletRecord>() {
            @mockit.Mock
            public CompletableFuture<String> get(Wallet wallet, String type, String id, String optionsJson) {
                assertEquals(userWallet, wallet);
                assertEquals("health", type);
                assertEquals("records", id);

                CompletableFuture<String> future = new CompletableFuture<>();
                future.complete(objectMapper.createObjectNode().put("test", listOnString).get("test").asText());
                return future;
            }
        };

        List<HealthRecord> result = healthDataService.getRecords(userWallet);

        assertEquals(records, result);
    }
}