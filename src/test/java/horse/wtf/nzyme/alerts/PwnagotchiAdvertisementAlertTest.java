package horse.wtf.nzyme.alerts;

import horse.wtf.nzyme.Subsystem;
import horse.wtf.nzyme.dot11.interceptors.misc.PwnagotchiAdvertisement;
import horse.wtf.nzyme.notifications.FieldNames;
import org.joda.time.DateTime;
import org.testng.annotations.Test;

import static org.testng.Assert.*;

public class PwnagotchiAdvertisementAlertTest extends AlertTestHelper {

    @Test
    public void testStandard() {
        PwnagotchiAdvertisementAlert a = PwnagotchiAdvertisementAlert.create(
                DateTime.now(),
                PwnagotchiAdvertisement.create("james", "1.0.0", "abc123", 60D, 1, 9001),
                1,
                1000,
                -50,
                1
        );

        // Wait a little to make lastSeen() assertions work.
        try {
            Thread.sleep(50);
        } catch (InterruptedException e) { /* noop */ }

        assertEquals(a.getName(), "james");
        assertEquals(a.getVersion(), "1.0.0");
        assertEquals(a.getIdentity(), "abc123");
        assertEquals(a.getUptime(), 60D);
        assertEquals(a.getFields().get(FieldNames.PWND_THIS_RUN), 1);
        assertEquals(a.getFields().get(FieldNames.PWND_TOTAL), 9001);
        assertEquals(a.getMessage(), "Pwnagotchi [james] with identity [abc123] and version [1.0.0] detected. Uptime [60.0].");

        assertEquals(a.getType(), Alert.TYPE.PWNAGOTCHI_ADVERTISEMENT);
        assertEquals(a.getSubsystem(), Subsystem.DOT_11);
        assertEquals(a.getFrameCount(), (Long) 1L);
        assertFalse(a.getLastSeen().isAfterNow());
        assertTrue(a.getLastSeen().isBeforeNow());
        assertFalse(a.getFirstSeen().isAfterNow());
        assertTrue(a.getFirstSeen().isBeforeNow());
        assertNotNull(a.getDocumentationLink());
        assertNotNull(a.getFalsePositives());
        assertNotNull(a.getDescription());
    }

    @Test
    public void testWorksWithNULLName() {
        PwnagotchiAdvertisementAlert a = PwnagotchiAdvertisementAlert.create(
                DateTime.now(),
                PwnagotchiAdvertisement.create(null, "1.0.0", "abc123", 60D, 1, 9001),
                1,
                1000,
                -50,
                1
        );

        assertEquals(a.getName(), "unknown");
        assertEquals(a.getMessage(), "Pwnagotchi [unknown] with identity [abc123] and version [1.0.0] detected. Uptime [60.0].");
    }

    @Test
    public void testWorksWithNULLVersion() {
        PwnagotchiAdvertisementAlert a = PwnagotchiAdvertisementAlert.create(
                DateTime.now(),
                PwnagotchiAdvertisement.create("james", null, "abc123", 60D, 1, 9001),
                1,
                1000,
                -50,
                1
        );

        assertEquals(a.getVersion(), "0");
        assertEquals(a.getMessage(), "Pwnagotchi [james] with identity [abc123] and version [0] detected. Uptime [60.0].");
    }

    @Test
    public void testWorksWithNULLIdentity() {
        PwnagotchiAdvertisementAlert a = PwnagotchiAdvertisementAlert.create(
                DateTime.now(),
                PwnagotchiAdvertisement.create("james", "1.0.0", null, 60D, 1, 9001),
                1,
                1000,
                -50,
                1
        );

        assertEquals(a.getIdentity(), "unknown");
        assertEquals(a.getMessage(), "Pwnagotchi [james] with identity [unknown] and version [1.0.0] detected. Uptime [60.0].");
    }

    @Test
    public void testWorksWithNULLUptime() {
        PwnagotchiAdvertisementAlert a = PwnagotchiAdvertisementAlert.create(
                DateTime.now(),
                PwnagotchiAdvertisement.create("james", "1.0.0", "abc123", null, 1, 9001),
                1,
                1000,
                -50,
                1
        );

        assertEquals(a.getUptime(), -1);
        assertEquals(a.getMessage(), "Pwnagotchi [james] with identity [abc123] and version [1.0.0] detected. Uptime [-1.0].");
    }

    @Test
    public void testWorksWithNULLPwndThisrun() {
        PwnagotchiAdvertisementAlert a = PwnagotchiAdvertisementAlert.create(
                DateTime.now(),
                PwnagotchiAdvertisement.create("james", "1.0.0", "abc123", 60D, null, 9001),
                1,
                1000,
                -50,
                1
        );

        assertEquals(a.getFields().get(FieldNames.PWND_THIS_RUN), -1);
        assertEquals(a.getMessage(), "Pwnagotchi [james] with identity [abc123] and version [1.0.0] detected. Uptime [60.0].");
    }

    @Test
    public void testWorksWithNULLPwndTotal() {
        PwnagotchiAdvertisementAlert a = PwnagotchiAdvertisementAlert.create(
                DateTime.now(),
                PwnagotchiAdvertisement.create("james", "1.0.0", "abc123", 60D, 1, null),
                1,
                1000,
                -50,
                1
        );

        assertEquals(a.getFields().get(FieldNames.PWND_TOTAL), -1);
        assertEquals(a.getMessage(), "Pwnagotchi [james] with identity [abc123] and version [1.0.0] detected. Uptime [60.0].");
    }

    @Test
    public void testWorksWithNULLAllOverEverythingOMG() {
        PwnagotchiAdvertisementAlert a = PwnagotchiAdvertisementAlert.create(
                DateTime.now(),
                PwnagotchiAdvertisement.create(null, null, null, null, null, null),
                1,
                1000,
                -50,
                1
        );

        assertEquals(a.getName(), "unknown");
        assertEquals(a.getVersion(), "0");
        assertEquals(a.getIdentity(), "unknown");
        assertEquals(a.getUptime(), -1);
        assertEquals(a.getFields().get(FieldNames.PWND_THIS_RUN), -1);
        assertEquals(a.getFields().get(FieldNames.PWND_TOTAL), -1);
        assertEquals(a.getMessage(), "Pwnagotchi [unknown] with identity [unknown] and version [0] detected. Uptime [-1.0].");
    }

}