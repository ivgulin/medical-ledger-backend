package com.mokujin.ssi.service.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.mokujin.ssi.model.internal.Role;
import com.mokujin.ssi.model.user.response.Auth;
import com.mokujin.ssi.service.WalletService;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import mockit.Mock;
import mockit.MockUp;
import org.hyperledger.indy.sdk.IndyException;
import org.hyperledger.indy.sdk.did.Did;
import org.hyperledger.indy.sdk.wallet.Wallet;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.concurrent.CompletableFuture;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;

@Slf4j
class WalletServiceImplTest {

    private ObjectMapper objectMapper = new ObjectMapper();

    private WalletService walletService = new WalletServiceImpl(objectMapper);

    @Test
    @SneakyThrows
    void getOrCreateWallet_validInputs_walletIsReturned() {

        Wallet wallet = mock(Wallet.class);

        new MockUp<Wallet>() {
            @Mock
            public CompletableFuture<Wallet> openWallet(String config, String credentials) {
                CompletableFuture<Wallet> future = new CompletableFuture<>();
                future.complete(wallet);
                return future;
            }
        };

        Wallet result = walletService.getOrCreateWallet("test", "test");
        assertEquals(wallet, result);
    }

    @Test
    @SneakyThrows
    void getOrCreateWallet_walletDoesNotExist_newWalletIsReturned() {

        IndyException indyException = mock(IndyException.class);
        Wallet wallet = mock(Wallet.class);

        new MockUp<Wallet>() {
            boolean doesThrow = true;

            @Mock
            public CompletableFuture<Wallet> openWallet(String config, String credentials) throws IndyException {
                if (doesThrow) {
                    doesThrow = false;
                    throw indyException;
                }
                CompletableFuture<Wallet> future = new CompletableFuture<>();
                future.complete(wallet);
                return future;
            }

            @Mock
            public CompletableFuture<Void> createWallet(String config, String credentials) {
                return CompletableFuture.runAsync(() -> log.debug("In mock."));
            }
        };

        Wallet result = walletService.getOrCreateWallet("test", "test");
        assertEquals(wallet, result);
    }

    @ParameterizedTest
    @MethodSource("provideKeysAndResultExpectations")
    void doesWalletExist_validInputs_walletIsReturned(String publicKey, String privateKey, Auth expectedAuth) {

        Wallet doctorWallet = mock(Wallet.class);
        Wallet patientWallet = mock(Wallet.class);

        new MockUp<Wallet>() {
            @Mock
            public CompletableFuture<Wallet> openWallet(String config, String credentials) throws Exception {
                CompletableFuture<Wallet> future = new CompletableFuture<>();

                if (config.equals("{\"id\":\"invalid\"}")) throw new Exception();
                if (config.equals("{\"id\":\"doctor\"}")) future.complete(doctorWallet);
                if (config.equals("{\"id\":\"patient\"}")) future.complete(patientWallet);

                return future;
            }
        };

        new MockUp<Did>() {
            @mockit.Mock
            public CompletableFuture<String> getListMyDidsWithMeta(Wallet wallet) {
                ArrayNode contacts = objectMapper.createArrayNode();

                if (wallet.equals(doctorWallet)) {
                    ObjectNode verinym = objectMapper.createObjectNode();
                    verinym.put("contactName", "John Doe");
                    verinym.put("verinym", true);

                    String verinymInString = verinym.toString();

                    ObjectNode verinymData = objectMapper.createObjectNode();
                    verinymData.put("did", "did");
                    verinymData.put("verkey", "some verkey");
                    verinymData.put("tempVerkey", "some temporal verkey");
                    verinymData.put("metadata", verinymInString);

                    contacts.add(verinymData);
                }

                CompletableFuture<String> future = new CompletableFuture<>();
                future.complete(contacts.toString());

                return future;
            }
        };

        Auth auth = walletService.doesWalletExist(publicKey, privateKey);
        assertEquals(expectedAuth, auth);
    }

    private static Stream<Arguments> provideKeysAndResultExpectations() {


        return Stream.of(
                Arguments.of("invalid", "valid", new Auth(false, null)),
                Arguments.of("doctor", "valid", new Auth(true, Role.DOCTOR)),
                Arguments.of("patient", "valid", new Auth(true, Role.PATIENT))
        );
    }
}
