package horse.wtf.nzyme.dot11.networks.sentry;

import horse.wtf.nzyme.MockNzyme;
import horse.wtf.nzyme.NzymeLeader;
import org.joda.time.DateTime;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import static org.testng.Assert.*;

public class SentryTest {

    @BeforeMethod
    public void cleanSentry() {
        NzymeLeader nzyme = new MockNzyme();
        nzyme.getDatabase().useHandle(handle -> handle.execute("DELETE FROM sentry_ssids;"));
    }

    @Test
    public void testTickSSID() throws InterruptedException {
        NzymeLeader nzyme = new MockNzyme(1);
        Sentry sentry = nzyme.getSentry();

        assertFalse(sentry.knowsSSID("foo1"));
        assertFalse(sentry.knowsSSID("foo2"));

        assertEquals(sentry.getSSIDs().size(), 0);

        sentry.tickSSID("foo1", DateTime.now());
        assertEquals(sentry.getSSIDs().size(), 1);

        sentry.tickSSID("foo2", DateTime.now());
        assertEquals(sentry.getSSIDs().size(), 2);

        sentry.tickSSID("foo1", DateTime.now());
        assertEquals(sentry.getSSIDs().size(), 2);

        assertTrue(sentry.knowsSSID("foo1"));
        assertTrue(sentry.knowsSSID("foo2"));
        assertFalse(sentry.knowsSSID("bar"));

        sentry.stop();

        Thread.sleep(2500);

        Sentry sentry2 = new Sentry(new MockNzyme(), 1);
        assertEquals(sentry.getSSIDs().size(), 2);

        assertTrue(sentry.knowsSSID("foo1"));
        assertTrue(sentry.knowsSSID("foo2"));
        assertFalse(sentry.knowsSSID("bar"));

        sentry2.stop();
    }

}