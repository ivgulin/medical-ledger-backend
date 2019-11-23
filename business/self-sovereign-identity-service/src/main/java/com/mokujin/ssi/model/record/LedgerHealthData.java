package com.mokujin.ssi.model.record;

import lombok.Data;

import java.util.List;

@Data
public class LedgerHealthData {

    private String id;

    private List<HealthRecord> value;
}
