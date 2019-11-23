package com.mokujin.user.model.record.impl;

import com.mokujin.user.model.record.HealthRecord;
import lombok.Data;
import lombok.EqualsAndHashCode;

import static com.mokujin.user.model.record.HealthRecord.Type.body;

@Data
@EqualsAndHashCode(callSuper = true)
public class BodyMeasurement extends HealthRecord {

    private long date;

    private float height;

    private float weight;

    private float basalTemperature;

    private float averageTemperature;

    private float waistCircumference;

    public BodyMeasurement() {
        super(body.name());
    }

    public BodyMeasurement(float height, float weight, float basalTemperature,
                           float averageTemperature, float waistCircumference) {
        super(body.name());
        this.height = height;
        this.weight = weight;
        this.basalTemperature = basalTemperature;
        this.averageTemperature = averageTemperature;
        this.waistCircumference = waistCircumference;
    }
}
