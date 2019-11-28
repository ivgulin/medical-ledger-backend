package com.mokujin.user.service;

import com.mokujin.user.model.User;
import com.mokujin.user.model.record.HealthRecord;

import java.util.List;

public interface HealthDataService {

    List<HealthRecord> save(String publicKey, String privateKey, HealthRecord healthRecord);

    User share(String publicKey, String privateKey, HealthRecord healthRecord, String doctorNumber);

}
