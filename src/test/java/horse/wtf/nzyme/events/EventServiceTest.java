package horse.wtf.nzyme.events;

import horse.wtf.nzyme.MockNzyme;
import horse.wtf.nzyme.NzymeLeader;
import org.joda.time.DateTime;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.util.concurrent.atomic.AtomicBoolean;

import static org.testng.Assert.*;

public class EventServiceTest {

    @BeforeMethod
    public void cleanEvents() {
        NzymeLeader nzyme = new MockNzyme();
        nzyme.getDatabase().useHandle(handle -> handle.execute("DELETE FROM events"));
    }

    @Test
    public void testRecordEvent() {
        NzymeLeader nzyme = new MockNzyme();
        EventService events = nzyme.getEventService();

        assertEquals(events.countAll(), 0);
        events.recordEvent(new StartupEvent());
        assertEquals(events.countAll(), 1);
        events.recordEvent(new StartupEvent());
        assertEquals(events.countAll(), 2);
        events.recordEvent(new ShutdownEvent());
        assertEquals(events.countAll(), 3);
    }

    @Test
    public void testFindAllEventsOfLast24Hours() {
        NzymeLeader nzyme = new MockNzyme();
        EventService events = nzyme.getEventService();

        assertEquals(events.countAll(), 0);
        assertEquals(events.findAllEventsOfLast24Hours().size(), 0);
        events.recordEvent(new StartupEvent());
        assertEquals(events.countAll(), 1);
        assertEquals(events.findAllEventsOfLast24Hours().size(), 1);

        events.recordEvent(new StartupEvent(), DateTime.now().minusHours(23));
        assertEquals(events.countAll(), 2);
        assertEquals(events.findAllEventsOfLast24Hours().size(), 2);

        events.recordEvent(new StartupEvent(), DateTime.now().minusHours(30));
        assertEquals(events.countAll(), 3);
        assertEquals(events.findAllEventsOfLast24Hours().size(), 2);

        events.recordEvent(new StartupEvent(), DateTime.now().minusHours(5));
        assertEquals(events.countAll(), 4);
        assertEquals(events.findAllEventsOfLast24Hours().size(), 3);
    }

    @Test
    public void testCountAllOfTypeOfLast24Hours() {
        NzymeLeader nzyme = new MockNzyme();
        EventService events = nzyme.getEventService();

        assertEquals(events.countAllOfTypeOfLast24Hours(Event.TYPE.SHUTDOWN), 0);
        assertEquals(events.countAllOfTypeOfLast24Hours(Event.TYPE.STARTUP), 0);

        events.recordEvent(new ShutdownEvent());
        assertEquals(events.countAllOfTypeOfLast24Hours(Event.TYPE.SHUTDOWN), 1);
        assertEquals(events.countAllOfTypeOfLast24Hours(Event.TYPE.STARTUP), 0);

        events.recordEvent(new StartupEvent());
        assertEquals(events.countAllOfTypeOfLast24Hours(Event.TYPE.SHUTDOWN), 1);
        assertEquals(events.countAllOfTypeOfLast24Hours(Event.TYPE.STARTUP), 1);

        events.recordEvent(new ShutdownEvent());
        assertEquals(events.countAllOfTypeOfLast24Hours(Event.TYPE.SHUTDOWN), 2);
        assertEquals(events.countAllOfTypeOfLast24Hours(Event.TYPE.STARTUP), 1);

        events.recordEvent(new ShutdownEvent(), DateTime.now().minusHours(30));
        assertEquals(events.countAllOfTypeOfLast24Hours(Event.TYPE.SHUTDOWN), 2);
        assertEquals(events.countAllOfTypeOfLast24Hours(Event.TYPE.STARTUP), 1);
    }

    @Test
    public void testSubscribe() {
        NzymeLeader nzyme = new MockNzyme();
        EventService events = nzyme.getEventService();

        assertEquals(events.getSubscribers().size(), 0);
        assertNull(events.getSubscribers().get(Event.TYPE.SHUTDOWN));
        assertNull(events.getSubscribers().get(Event.TYPE.BROKEN_PROBE));

        AtomicBoolean b1 = new AtomicBoolean(false);
        events.subscribe(Event.TYPE.BROKEN_PROBE, event -> b1.set(true));
        assertEquals(events.getSubscribers().size(), 1);
        assertNull(events.getSubscribers().get(Event.TYPE.SHUTDOWN));
        assertEquals(events.getSubscribers().get(Event.TYPE.BROKEN_PROBE).size(), 1);

        AtomicBoolean b2 = new AtomicBoolean(false);
        events.subscribe(Event.TYPE.BROKEN_PROBE, event -> b2.set(true));
        assertEquals(events.getSubscribers().size(), 1);
        assertNull(events.getSubscribers().get(Event.TYPE.SHUTDOWN));
        assertEquals(events.getSubscribers().get(Event.TYPE.BROKEN_PROBE).size(), 2);

        AtomicBoolean b3 = new AtomicBoolean(false);
        events.subscribe(Event.TYPE.SHUTDOWN, event -> b3.set(true));
        assertEquals(events.getSubscribers().size(), 2);
        assertEquals(events.getSubscribers().get(Event.TYPE.SHUTDOWN).size(), 1);
        assertEquals(events.getSubscribers().get(Event.TYPE.BROKEN_PROBE).size(), 2);

        assertEquals(b1.get(), false);
        assertEquals(b2.get(), false);
        assertEquals(b3.get(), false);

        events.recordEvent(new BrokenProbeEvent("foo", "bar"));
        assertEquals(b1.get(), true);
        assertEquals(b2.get(), true);
        assertEquals(b3.get(), false);

        events.recordEvent(new ShutdownEvent());
        assertEquals(b1.get(), true);
        assertEquals(b2.get(), true);
        assertEquals(b3.get(), true);
    }

}