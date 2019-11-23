package com.mokujin.ssi.model.record;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.mokujin.ssi.model.record.impl.BodyMeasurement;
import com.mokujin.ssi.model.record.impl.HeartHealthRecord;
import lombok.AllArgsConstructor;
import lombok.Data;

import static com.fasterxml.jackson.annotation.JsonTypeInfo.As.EXISTING_PROPERTY;
import static com.fasterxml.jackson.annotation.JsonTypeInfo.Id.NAME;

@Data
@AllArgsConstructor
@JsonTypeInfo(use = NAME, property = "type", include = EXISTING_PROPERTY)
@JsonSubTypes({
        @JsonSubTypes.Type(value = HeartHealthRecord.class, name = "heart"),
        @JsonSubTypes.Type(value = BodyMeasurement.class, name = "body")
})
public class HealthRecord {

    private String type;

    public enum Type {
        heart,
        body
    }

}