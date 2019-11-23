package com.mokujin.user.model.record.impl;

import com.mokujin.user.model.record.HealthRecord;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import static com.mokujin.user.model.record.HealthRecord.Type.heart;

@Data
@EqualsAndHashCode(callSuper = true)
public class HeartHealthRecord extends HealthRecord {

    private long date;

    private Rate rate;

    private BloodPressure pressure;

    private int oxygenSaturation;

    private int peripheralPerfusionIndex;

    public HeartHealthRecord() {
        super(heart.name());
    }

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    class Rate {

        private int walking;

        private int resting;

        private int workout;

        private int sleep;

        private int breathe;

    }

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    class BloodPressure {

        private int systolic;

        private int diastolic;

    }
}
