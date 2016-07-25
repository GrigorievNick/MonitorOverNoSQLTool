package org.mhr.monitor.model;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;

public class SerializeUtils {

    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    public static String toJson(Event event)  {
        try {
            return OBJECT_MAPPER.writeValueAsString(event);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }

    public static Event fromJson(String event) {
        try {
            return OBJECT_MAPPER.readValue(event, Event.class);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }


}
