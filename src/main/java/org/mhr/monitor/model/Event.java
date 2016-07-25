package org.mhr.monitor.model;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import lombok.EqualsAndHashCode;
import lombok.Getter;

@JsonTypeInfo(use= JsonTypeInfo.Id.NAME, include= JsonTypeInfo.As.PROPERTY, property="type")
@JsonSubTypes(value = {
    @JsonSubTypes.Type(value = CommandEvent.class, name = "Command"),
    @JsonSubTypes.Type(value = DataEvent.class, name = "Data")
})
@Getter
@EqualsAndHashCode
public abstract class Event {

    public Event() {
    }

}
