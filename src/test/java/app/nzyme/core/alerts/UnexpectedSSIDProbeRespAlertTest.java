package app.nzyme.core.alerts;

import app.nzyme.core.Subsystem;
import org.joda.time.DateTime;
import org.testng.annotations.Test;

import static org.testng.Assert.*;

public class UnexpectedSSIDProbeRespAlertTest extends AlertTestHelper {

    @Test
    public void testAlertStandard() {
        UnexpectedSSIDProbeRespAlert a = UnexpectedSSIDProbeRespAlert.create(
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
        assertEquals(a.getMessage(), "Our BSSID [00:c0:ca:95:68:3b] advertised unexpected SSID [wtf] with probe response frame.");
        assertEquals(a.getType(), Alert.TYPE.UNEXPECTED_SSID_PROBERESP);
        assertEquals(a.getSubsystem(), Subsystem.DOT_11);
        assertEquals(a.getFrameCount(), (Long) 1L);
        assertFalse(a.getLastSeen().isAfterNow());
        assertTrue(a.getLastSeen().isBeforeNow());
        assertFalse(a.getFirstSeen().isAfterNow());
        assertTrue(a.getFirstSeen().isBeforeNow());
        assertNotNull(a.getDocumentationLink());
        assertNotNull(a.getFalsePositives());
        assertNotNull(a.getDescription());

        UnexpectedSSIDProbeRespAlert a2 = UnexpectedSSIDProbeRespAlert.create(
                DateTime.now(),
                "wtf",
                "00:c0:ca:95:68:3b",
                1,
                1000,
                -50,
                1
        );

        assertTrue(a.sameAs(a2));

        UnexpectedSSIDProbeRespAlert a3 = UnexpectedSSIDProbeRespAlert.create(
                DateTime.now(),
                "wtfDIFF",
                "00:c0:ca:95:68:3b",
                1,
                1000,
                -50,
                1
        );

        UnexpectedSSIDProbeRespAlert a4 = UnexpectedSSIDProbeRespAlert.create(
                DateTime.now(),
                "wtf",
                "0a:c0:ca:95:68:3b",
                1,
                1000,
                -50,
                1
        );

        assertFalse(a.sameAs(a3));
        assertFalse(a.sameAs(a4));

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
        UnexpectedSSIDProbeRespAlert.create(
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
        UnexpectedSSIDProbeRespAlert.create(
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