package com.mokujin.ssi.service.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.mokujin.ssi.model.exception.extention.LedgerException;
import com.mokujin.ssi.model.record.HealthRecord;
import com.mokujin.ssi.model.record.LedgerHealthData;
import com.mokujin.ssi.service.HealthDataService;
import com.mokujin.ssi.service.WalletService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.hyperledger.indy.sdk.non_secrets.WalletRecord;
import org.hyperledger.indy.sdk.wallet.Wallet;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

import static org.springframework.http.HttpStatus.INTERNAL_SERVER_ERROR;

@Slf4j
@Service
@RequiredArgsConstructor
public class HealthDataServiceImpl implements HealthDataService {

    private final WalletService walletService;
    private final ObjectMapper objectMapper;

    @Override
    public List<HealthRecord> save(String publicKey, String privateKey, HealthRecord healthRecord) {
        try (Wallet wallet = walletService.getOrCreateWallet(publicKey, privateKey);) {

            List<HealthRecord> records = this.getRecords(wallet);
            records.add(healthRecord);

            String recordsInString = objectMapper.writeValueAsString(records);
            WalletRecord.updateValue(wallet, "health", "records", recordsInString);

            return records;
        } catch (Exception e) {
            log.error("Exception was thrown: " + e);
            throw new LedgerException(INTERNAL_SERVER_ERROR, e.getMessage());
        }
    }

    @Override
    public List<HealthRecord> getRecords(Wallet wallet) throws Exception {
        List<HealthRecord> records;
        try {
            String recordsInString = WalletRecord.get(wallet, "health", "records", "{}").get()
                    .replace("\\", "")
                    .replace("\"{", "{")
                    .replace("}\"", "}");

            log.info("'recordsInString={}'", recordsInString);
            records = objectMapper.readValue(recordsInString, LedgerHealthData.class).getValue();
            return records;
        } catch (Exception e) {
            records = new ArrayList<>();
            String recordsInString = objectMapper.writeValueAsString(records);
            WalletRecord.add(wallet, "health", "records", recordsInString, "{}");
            return records;
        }
    }
}
