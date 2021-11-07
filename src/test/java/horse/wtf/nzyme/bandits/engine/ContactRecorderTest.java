package horse.wtf.nzyme.bandits.engine;

import horse.wtf.nzyme.MockNzyme;
import horse.wtf.nzyme.NzymeLeader;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import static org.testng.Assert.*;

public class ContactRecorderTest {

    @BeforeMethod
    public void cleanAlerts() {
        NzymeLeader nzyme = new MockNzyme();
        nzyme.getDatabase().useHandle(handle -> handle.execute("DELETE FROM bandits;"));
    }

    @Test
    public void testRecordFrame() throws InterruptedException {
        NzymeLeader nzyme = new MockNzyme();
        ContactRecorder rec = new ContactRecorder(5, nzyme);

        assertTrue(rec.getSSIDs().isEmpty());
        assertTrue(rec.getBSSIDs().isEmpty());

        UUID u1 = UUID.randomUUID();
        rec.recordFrame(u1, -50, "6B:29:AF:99:17:20", Optional.of("foo"));
        rec.recordFrame(u1, -53, "6B:29:AF:99:17:20", Optional.of("foo"));
        rec.recordFrame(u1, -41, "9A:87:13:D6:CA:F4", Optional.of("foo"));
        rec.recordFrame(u1, -23, "6B:29:AF:99:17:20", Optional.of("bar"));
        rec.recordFrame(u1, -19, "6B:29:AF:99:17:20", Optional.of("bar"));
        rec.recordFrame(u1, -12, "6B:29:AF:99:17:20", Optional.empty());
        rec.recordFrame(u1, -49, "9A:87:13:D6:CA:F4", Optional.empty());

        assertEquals(rec.getSSIDs().size(), 1);
        assertEquals(rec.getBSSIDs().size(), 1);

        UUID u2 = UUID.randomUUID();
        rec.recordFrame(u2, -23, "6B:29:AF:99:17:20", Optional.empty());
        rec.recordFrame(u2, -19, "6B:29:AF:99:17:20", Optional.empty());

        assertEquals(rec.getSSIDs().size(), 1);
        assertEquals(rec.getBSSIDs().size(), 2);

        UUID u3 = UUID.randomUUID();
        rec.recordFrame(u3, -90, "12:0C:CD:FD:5F:E3", Optional.of("baz"));
        rec.recordFrame(u3, -80, "12:0C:CD:FD:5F:E3", Optional.of("baz"));

        assertEquals(rec.getSSIDs().size(), 2);
        assertEquals(rec.getBSSIDs().size(), 3);

        assertEquals(rec.getSSIDs().get(u1).size(), 2);
        assertEquals(rec.getSSIDs().get(u1).get("foo").size(), 3);
        assertEquals(rec.getSSIDs().get(u1).get("bar").size(), 2);

        assertEquals(rec.getBSSIDs().get(u1).size(), 2);
        assertEquals(rec.getBSSIDs().get(u1).get("6B:29:AF:99:17:20").size(), 5);
        assertEquals(rec.getBSSIDs().get(u1).get("9A:87:13:D6:CA:F4").size(), 2);

        assertNull(rec.getSSIDs().get(u2));

        assertEquals(rec.getBSSIDs().get(u2).size(), 1);
        assertEquals(rec.getBSSIDs().get(u2).get("6B:29:AF:99:17:20").size(), 2);

        assertEquals(rec.getSSIDs().get(u3).size(), 1);
        assertEquals(rec.getSSIDs().get(u3).get("baz").size(), 2);

        assertEquals(rec.getSSIDs().get(u3).size(), 1);
        assertEquals(rec.getSSIDs().get(u3).get("baz").size(), 2);

        assertEquals(rec.getBSSIDs().get(u3).size(), 1);
        assertEquals(rec.getBSSIDs().get(u3).get("12:0C:CD:FD:5F:E3").size(), 2);

        Map<UUID, Map<String, ContactRecorder.ComputationResult>> ssidCompute = ContactRecorder.compute(rec.getSSIDs());
        Map<UUID, Map<String, ContactRecorder.ComputationResult>> bssidCompute = ContactRecorder.compute(rec.getBSSIDs());

        assertEquals(ssidCompute.size(), 2);

        assertEquals(ssidCompute.get(u1).size(), 2);
        assertEquals(ssidCompute.get(u1).get("foo"), ContactRecorder.ComputationResult.create(-48.0D, 5.0990195135927845D));
        assertEquals(ssidCompute.get(u1).get("bar"), ContactRecorder.ComputationResult.create(-21.0D, 2.0D));

        assertEquals(ssidCompute.get(u3).size(), 1);
        assertEquals(ssidCompute.get(u3).get("baz"), ContactRecorder.ComputationResult.create(-85.0D, 5.D));

        assertEquals(bssidCompute.size(), 3);

        assertEquals(bssidCompute.get(u1).size(), 2);
        assertEquals(bssidCompute.get(u1).get("6B:29:AF:99:17:20"), ContactRecorder.ComputationResult.create(-31.4D, 16.81190054693401D));
        assertEquals(bssidCompute.get(u1).get("9A:87:13:D6:CA:F4"), ContactRecorder.ComputationResult.create(-45.0D, 4.0D));

        assertEquals(bssidCompute.get(u2).size(), 1);
        assertEquals(bssidCompute.get(u2).get("6B:29:AF:99:17:20"), ContactRecorder.ComputationResult.create(-21.0D, 2.0D));

        assertEquals(bssidCompute.get(u3).size(), 1);
        assertEquals(bssidCompute.get(u3).get("12:0C:CD:FD:5F:E3"), ContactRecorder.ComputationResult.create(-85.D, 5.0D));
    }

    @Test
    public void testCleansFrames() throws InterruptedException {
        ContactRecorder rec = new ContactRecorder(3, new MockNzyme());

        assertTrue(rec.getSSIDs().isEmpty());
        assertTrue(rec.getBSSIDs().isEmpty());

        UUID u1 = UUID.randomUUID();

        rec.recordFrame(u1, -50, "6B:29:AF:99:17:20", Optional.of("foo"));
        rec.recordFrame(u1, -53, "6B:29:AF:99:17:20", Optional.of("foo"));
        assertEquals(rec.getSSIDs().size(), 1);
        assertEquals(rec.getBSSIDs().size(), 1);

        Thread.sleep(4000);

        assertTrue(rec.getSSIDs().isEmpty());
        assertTrue(rec.getBSSIDs().isEmpty());

        rec.recordFrame(u1, -50, "6B:29:AF:99:17:20", Optional.of("foo"));
        rec.recordFrame(u1, -53, "6B:29:AF:99:17:20", Optional.of("foo"));
        assertEquals(rec.getSSIDs().size(), 1);
        assertEquals(rec.getBSSIDs().size(), 1);
    }
}