package org.mhr.monitor.model;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonRootName;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import java.util.Date;
import java.util.Set;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;

import static java.util.Collections.emptySet;

@Getter
@EqualsAndHashCode(callSuper = true)
@ToString
@JsonRootName("Command")
@JsonDeserialize(using = JsonDeserializer.None.class)
public class CommandEvent extends Event {

    public enum Command {START, STOP, START_DONE, STOP_DONE, ERROR}

    private Command command;
    private Set<String> fields;
    private OperationType operationType;
    private Integer page;
    private Integer pageSize;
    private ResolvableDateTime start;
    private ResolvableDateTime end;

    @JsonCreator(mode = JsonCreator.Mode.DEFAULT)
    public CommandEvent(@JsonProperty("command") Command command,
                        @JsonProperty("fields") Set<String> fields,
                        @JsonProperty("operationType") OperationType operationType,
                        @JsonProperty("page") Integer page,
                        @JsonProperty("pageSize") Integer pageSize,
                        @JsonProperty("start") ResolvableDateTime start,
                        @JsonProperty("end") ResolvableDateTime end) {
        this.command = command;
        this.fields = fields;
        this.operationType = operationType;
        this.page = page;
        this.pageSize = pageSize;
        if((start != null && end != null) &&
            (start.live || start.date.compareTo(end.date) >= 0))
            throw new IllegalArgumentException("Stat date >= then end date!");
        this.start = start;
        this.end = end;
    }

    public CommandEvent(Command command, Set<String> fields, OperationType operationType) {
        this.command = command;
        this.fields = fields;
        this.operationType = operationType;
    }

    public CommandEvent(Command command) {
        this.command = command;
        this.fields = emptySet();
    }

    @EqualsAndHashCode
    @ToString
    @JsonDeserialize(using = JsonDeserializer.None.class)
    @Getter
    public static class ResolvableDateTime {

        @JsonSerialize
        private final Date date;

        @JsonSerialize
        private final boolean live;

        @JsonCreator(mode = JsonCreator.Mode.DEFAULT)
        public ResolvableDateTime(@JsonProperty("date") Date date, @JsonProperty("live") boolean live) {
            this.date = date;
            this.live = live;
        }
    }

}
