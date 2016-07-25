import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import org.mhr.monitor.model.CommandEvent;
import org.mhr.monitor.model.DataEvent;
import org.mhr.monitor.model.Event;
import org.mhr.monitor.model.Msg;
import org.mhr.monitor.model.OperationType;
import org.mhr.monitor.model.SerializeUtils;
import org.testng.annotations.Test;

import static com.google.common.collect.ImmutableMap.of;
import static java.util.Collections.singletonList;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNull;
import static org.testng.Assert.assertTrue;

public class SerializeUtilsTest {

    @Test
    public void testCommand() throws IOException {
        final CommandEvent actual =
            new CommandEvent(CommandEvent.Command.START, singletonList("field1"), OperationType.WIDTHRAW);
        System.out.println(new ObjectMapper().writeValueAsString(actual));
        final String s = SerializeUtils.toJson(actual);
        final Event expected = SerializeUtils.fromJson(s);
        assertEquals(actual, expected);
        assertTrue(actual instanceof CommandEvent);
        assertEquals((CommandEvent) actual, (CommandEvent) expected);
    }

    @Test
    public void testData() throws IOException {
        final DataEvent actual =
            new DataEvent(new Msg(0L, OperationType.WIDTHRAW, of("field", "value")));
        System.out.print(new ObjectMapper().writeValueAsString(actual));
        final String s = SerializeUtils.toJson(actual);
        final Event expected = SerializeUtils.fromJson(s);
        assertEquals(actual, expected);
        assertTrue(actual instanceof DataEvent);
        assertEquals((DataEvent) actual, (DataEvent) expected);
    }

    @Test
    public void testPartialDeserialise() {
        final String input =
            "{\"type\":\"Command\",\"command\":\"START\",\"fields\":[\"field1\"],\"operationType\":\"WIDTHRAW\"}";

        final Event event = SerializeUtils.fromJson(input);
        assertTrue(event instanceof CommandEvent);
        assertEquals(((CommandEvent) event).getCommand(), CommandEvent.Command.START);
        assertEquals(((CommandEvent) event).getFields(), singletonList("field1"));
        assertEquals(((CommandEvent) event).getOperationType(), OperationType.WIDTHRAW);
        assertNull(((CommandEvent) event).getPage());
        assertNull(((CommandEvent) event).getPageSize());
    }
}
