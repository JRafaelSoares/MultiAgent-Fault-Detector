import org.junit.jupiter.api.Test;

import java.util.ArrayList;

import static org.junit.jupiter.api.Assertions.*;

class NetworkSimulatorTest {

    @Test
    void successSimple(){
        NetworkSimulator networkSimulator = new NetworkSimulator();

        networkSimulator.writeBuffer("1", new Message("1", Message.Type.pingRequest));
        ArrayList<Message> messages = networkSimulator.readBuffer("1");

        assertNotNull(messages);
        assertEquals(1, messages.size());
        assertEquals("1", messages.get(0).getId());
        assertEquals(Message.Type.pingRequest, messages.get(0).getType());

        messages = networkSimulator.readBuffer("1");

        assertNull(messages);
    }

    @Test
    void successComplex(){
        NetworkSimulator networkSimulator = new NetworkSimulator();

        networkSimulator.writeBuffer("1", new Message("S1", Message.Type.pingRequest));
        networkSimulator.writeBuffer("2", new Message("S2", Message.Type.pingRequest));
        networkSimulator.writeBuffer("2", new Message("S22", Message.Type.pingResponse));
        networkSimulator.writeBuffer("3", new Message("S3", Message.Type.pingRequest));
        networkSimulator.writeBuffer("1", new Message("S11", Message.Type.pingResponse));


        // id == 1
        ArrayList<Message> messages = networkSimulator.readBuffer("1");

        assertNotNull(messages);
        assertEquals(2, messages.size());
        assertEquals("S1", messages.get(0).getId());
        assertEquals(Message.Type.pingRequest, messages.get(0).getType());
        assertEquals("S11", messages.get(1).getId());
        assertEquals(Message.Type.pingResponse, messages.get(1).getType());

        messages = networkSimulator.readBuffer("1");

        assertNull(messages);

        // id == 2

        messages = networkSimulator.readBuffer("2");

        assertNotNull(messages);
        assertEquals(2, messages.size());
        assertEquals("S2", messages.get(0).getId());
        assertEquals(Message.Type.pingRequest, messages.get(0).getType());
        assertEquals("S22", messages.get(1).getId());
        assertEquals(Message.Type.pingResponse, messages.get(1).getType());

        messages = networkSimulator.readBuffer("2");

        assertNull(messages);

        // id == 3

        messages = networkSimulator.readBuffer("3");

        assertNotNull(messages);
        assertEquals(1, messages.size());
        assertEquals("S3", messages.get(0).getId());
        assertEquals(Message.Type.pingRequest, messages.get(0).getType());

        messages = networkSimulator.readBuffer("3");

        assertNull(messages);

    }

    @Test
    void successBroadcast() {
        NetworkSimulator networkSimulator = new NetworkSimulator();

        for(int i=0; i<10; i++){
            networkSimulator.registerFD("" + i);
        }

        networkSimulator.broadcastFDs(new Message("1", Message.Type.pingRequest));

        for(int i=0; i<10; i++){
            ArrayList<Message> messages = networkSimulator.readBuffer("" + i);
            assertNotNull(messages);
            assertEquals(1, messages.size());
            assertEquals(Message.Type.pingRequest, messages.get(0).getType());
            assertEquals("1", messages.get(0).getId());
        }
    }
}