package app.nzyme.core.alerts;

import app.nzyme.core.Subsystem;
import org.joda.time.DateTime;
import org.testng.annotations.Test;

import static org.testng.Assert.*;

public class UnexpectedChannelBeaconAlertTest extends AlertTestHelper {

    @Test
    public void testAlertStandard() {
        UnexpectedChannelBeaconAlert a = UnexpectedChannelBeaconAlert.create(
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
        assertEquals(a.getMessage(), "SSID [wtf] was advertised on an unexpected channel.");
        assertEquals(a.getType(), Alert.TYPE.UNEXPECTED_CHANNEL_BEACON);
        assertEquals(a.getSubsystem(), Subsystem.DOT_11);
        assertEquals(a.getFrameCount(), (Long) 1L);
        assertFalse(a.getLastSeen().isAfterNow());
        assertTrue(a.getLastSeen().isBeforeNow());
        assertFalse(a.getFirstSeen().isAfterNow());
        assertTrue(a.getFirstSeen().isBeforeNow());
        assertNotNull(a.getDocumentationLink());
        assertNotNull(a.getFalsePositives());
        assertNotNull(a.getDescription());

        UnexpectedChannelBeaconAlert a2 = UnexpectedChannelBeaconAlert.create(
                DateTime.now(),
                "wtf",
                "00:c0:ca:95:68:3e",
                1,
                1000,
                -50,
                1
        );

        assertTrue(a.sameAs(a2));

        UnexpectedChannelBeaconAlert a3 = UnexpectedChannelBeaconAlert.create(
                DateTime.now(),
                "wtfDIFF",
                "00:c0:ca:95:68:3b",
                1,
                1000,
                -50,
                1
        );

        UnexpectedChannelBeaconAlert a4 = UnexpectedChannelBeaconAlert.create(
                DateTime.now(),
                "wtf",
                "00:c0:ca:95:68:3b",
                1,
                1000,
                -50,
                1
        );

        assertFalse(a.sameAs(a3));
        assertTrue(a.sameAs(a4));

        UnexpectedBSSIDProbeRespAlert a6 = UnexpectedBSSIDProbeRespAlert.create(
                DateTime.now(),
                "wtf",
                "00:c0:ca:95:68:4b",
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
        UnexpectedChannelBeaconAlert.create(
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
        UnexpectedChannelBeaconAlert.create(
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