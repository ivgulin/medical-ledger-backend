package com.mokujin.ssi.service;

import com.mokujin.ssi.model.record.HealthRecord;
import org.hyperledger.indy.sdk.wallet.Wallet;

import java.util.List;

public interface HealthDataService {

    List<HealthRecord> save(String publicKey, String privateKey, HealthRecord healthRecord);

    List<HealthRecord> getRecords(Wallet wallet) throws Exception;

}
