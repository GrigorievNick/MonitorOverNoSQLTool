package org.mhr.monitor.model;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonRootName;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;

@Getter
@EqualsAndHashCode(callSuper = true)
@ToString
@JsonRootName("Data")
@JsonDeserialize(using = JsonDeserializer.None.class)
public class DataEvent extends Event {

    private final Msg msg;

    @JsonCreator(mode = JsonCreator.Mode.DEFAULT)
    public DataEvent(@JsonProperty("msg") Msg msg) {
        this.msg = msg;
    }
}
