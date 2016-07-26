package org.mhr.monitor.model;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonRootName;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import java.util.Map;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;

@Getter
@EqualsAndHashCode(callSuper = true)
@ToString
@JsonRootName("Data")
@JsonDeserialize(using = JsonDeserializer.None.class)
public class DataEvent extends Event{

    private final Long ts;
    private final OperationType operationType;
    private final Map<String, Object> operationBody;

    @JsonCreator(mode = JsonCreator.Mode.DEFAULT)
    public DataEvent(@JsonProperty("ts") Long ts,
               @JsonProperty("operationType") OperationType operationType,
               @JsonProperty("operationBody") Map<String, Object> operationBody) {
        this.ts = ts;
        this.operationType = operationType;
        this.operationBody = operationBody;
    }
}
