import org.junit.jupiter.api.Test;

import java.util.ArrayList;

import static org.junit.jupiter.api.Assertions.*;

class NetworkSimulatorTest {

    @Test
    void successSimple(){
        NetworkSimulator.writeBuffer("1", "message1");
        ArrayList<String> messages = NetworkSimulator.readBuffer("1");

        assertNotNull(messages);
        assertEquals(1, messages.size());
        assertEquals("message1", messages.get(0));

        messages = NetworkSimulator.readBuffer("1");

        assertNull(messages);
    }

    @Test
    void successComplex(){
        NetworkSimulator.writeBuffer("1", "message1");
        NetworkSimulator.writeBuffer("2", "message2");
        NetworkSimulator.writeBuffer("2", "message22");
        NetworkSimulator.writeBuffer("3", "message3");
        NetworkSimulator.writeBuffer("1", "message11");


        // id == 1
        ArrayList<String> messages = NetworkSimulator.readBuffer("1");

        assertNotNull(messages);
        assertEquals(2, messages.size());
        assertEquals("message1", messages.get(0));
        assertEquals("message11", messages.get(1));

        messages = NetworkSimulator.readBuffer("1");

        assertNull(messages);

        // id == 2

        messages = NetworkSimulator.readBuffer("2");

        assertNotNull(messages);
        assertEquals(2, messages.size());
        assertEquals("message2", messages.get(0));
        assertEquals("message22", messages.get(1));

        messages = NetworkSimulator.readBuffer("2");

        assertNull(messages);

        // id == 3

        messages = NetworkSimulator.readBuffer("3");

        assertNotNull(messages);
        assertEquals(1, messages.size());
        assertEquals("message3", messages.get(0));

        messages = NetworkSimulator.readBuffer("3");

        assertNull(messages);

    }
}