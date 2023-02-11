package app.nzyme.core.alerts;

import app.nzyme.core.Subsystem;
import org.joda.time.DateTime;
import org.testng.annotations.Test;

import static org.testng.Assert.*;

public class UnexpectedBSSIDBeaconAlertTest extends AlertTestHelper {

    @Test
    public void testAlertStandard() {
        UnexpectedBSSIDBeaconAlert a = UnexpectedBSSIDBeaconAlert.create(
                DateTime.now(),
                "wtf",
                "00:c0:ca:95:68:3b",
                1,
                1000,
                -50,
                1
        );

        // Wait a little to make lastSeen() assertions work.
        try {
            Thread.sleep(50);
        } catch (InterruptedException e) { /* noop */ }

        assertEquals(a.getSSID(), "wtf");
        assertEquals(a.getBSSID(), "00:c0:ca:95:68:3b");
        assertEquals(a.getMessage(), "SSID [wtf] was advertised with beacon frame by unexpected BSSID [00:c0:ca:95:68:3b].");
        assertEquals(a.getType(), Alert.TYPE.UNEXPECTED_BSSID_BEACON);
        assertEquals(a.getSubsystem(), Subsystem.DOT_11);
        assertEquals(a.getFrameCount(), (Long) 1L);
        assertFalse(a.getLastSeen().isAfterNow());
        assertTrue(a.getLastSeen().isBeforeNow());
        assertFalse(a.getFirstSeen().isAfterNow());
        assertTrue(a.getFirstSeen().isBeforeNow());
        assertNotNull(a.getDocumentationLink());
        assertNotNull(a.getFalsePositives());
        assertNotNull(a.getDescription());

        UnexpectedBSSIDBeaconAlert a2 = UnexpectedBSSIDBeaconAlert.create(
                DateTime.now(),
                "wtf",
                "00:c0:ca:95:68:3b",
                1,
                1000,
                -50,
                1
        );

        assertTrue(a.sameAs(a2));

        UnexpectedBSSIDBeaconAlert a3 = UnexpectedBSSIDBeaconAlert.create(
                DateTime.now(),
                "wtfNOTTHESAME",
                "00:c0:ca:95:68:3b",
                1,
                1000,
                -50,
                1
        );

        UnexpectedBSSIDBeaconAlert a4 = UnexpectedBSSIDBeaconAlert.create(
                DateTime.now(),
                "wtf",
                "00:c0:ca:95:68:3e",
                1,
                1000,
                -50,
                1
        );

        assertFalse(a.sameAs(a3));
        assertFalse(a.sameAs(a4));

        UnexpectedChannelBeaconAlert a6 = UnexpectedChannelBeaconAlert.create(
                DateTime.now(),
                "wtf",
                "00:c0:ca:95:68:4b",
                1,
                1000,
                -50,
                1
        );

        assertFalse(a.sameAs(a6));
    }

    @Test(expectedExceptions = IllegalArgumentException.class)
    public void testAlertHiddenSSID1() {
        UnexpectedBSSIDBeaconAlert.create(
                DateTime.now(),
                null,
                "00:c0:ca:95:68:3b",
                1,
                1000,
                -50,
                1
        );
    }

    @Test(expectedExceptions = IllegalArgumentException.class)
    public void testAlertHiddenSSID2() {
        UnexpectedBSSIDBeaconAlert.create(
                DateTime.now(),
                "",
                "00:c0:ca:95:68:3b",
                1,
                1000,
                -50,
                1
        );
    }

}