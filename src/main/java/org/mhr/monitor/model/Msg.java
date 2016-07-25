package org.mhr.monitor.model;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import java.util.Map;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@EqualsAndHashCode
@ToString
@JsonDeserialize(using = JsonDeserializer.None.class)
public class Msg {

    private final Long ts;
    private final OperationType operationType;
    private final Map<String, Object> operationBody;

    @JsonCreator(mode = JsonCreator.Mode.DEFAULT)
    public Msg(@JsonProperty("ts") Long ts,
               @JsonProperty("operationType") OperationType operationType,
               @JsonProperty("operationBody") Map<String, Object> operationBody) {
        this.ts = ts;
        this.operationType = operationType;
        this.operationBody = operationBody;
    }
}
