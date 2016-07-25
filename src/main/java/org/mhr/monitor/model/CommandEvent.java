package org.mhr.monitor.model;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonRootName;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import java.util.List;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;

import static java.util.Collections.emptyList;

@Getter
@EqualsAndHashCode(callSuper = true)
@ToString
@JsonRootName("Command")
@JsonDeserialize(using = JsonDeserializer.None.class)
public class CommandEvent extends Event {

    public enum Command {START, STOP, START_DONE, STOP_DONE, ERROR}

    private Command command;
    private List<String> fields;
    private OperationType operationType;
    private Integer page;
    private Integer pageSize;

    @JsonCreator(mode = JsonCreator.Mode.DEFAULT)
    public CommandEvent(@JsonProperty("command") Command command,
                        @JsonProperty("fields") List<String> fields,
                        @JsonProperty("operationType") OperationType operationType,
                        @JsonProperty("page") Integer page,
                        @JsonProperty("pageSize") Integer pageSize) {
        this.command = command;
        this.fields = fields;
        this.operationType = operationType;
        this.page = page;
        this.pageSize = pageSize;
    }

    public CommandEvent(Command command, List<String> fields, OperationType operationType) {
        this.command = command;
        this.fields = fields;
        this.operationType = operationType;
        this.page = page;
        this.pageSize = pageSize;
    }

    public CommandEvent(Command command) {
        this.command = command;
        this.fields = emptyList();
    }

}
