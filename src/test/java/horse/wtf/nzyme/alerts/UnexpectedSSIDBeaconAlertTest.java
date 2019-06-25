package horse.wtf.nzyme.alerts;

import horse.wtf.nzyme.Subsystem;
import org.testng.annotations.Test;

import static org.testng.Assert.*;

public class UnexpectedSSIDBeaconAlertTest extends AlertTestHelper {

    @Test
    public void testAlertStandard() {
        UnexpectedSSIDBeaconAlert a = UnexpectedSSIDBeaconAlert.create(
                "wtf",
                "00:c0:ca:95:68:3b",
                META_NO_WEP,
                buildMockProbe(BANDITS_STANDARD)
        );

        // Wait a little to make lastSeen() assertions work.
        try {
            Thread.sleep(50);
        } catch (InterruptedException e) { /* noop */ }

        assertEquals(a.getSSID(), "wtf");
        assertEquals(a.getMessage(), "Our BSSID [00:c0:ca:95:68:3b] advertised unexpected SSID [wtf] with beacon frame.");
        assertEquals(a.getType(), Alert.Type.UNEXPECTED_SSID_BEACON);
        assertEquals(a.getSubsystem(), Subsystem.DOT_11);
        assertEquals(a.getFrameCount(), 1);
        assertFalse(a.getLastSeen().isAfterNow());
        assertTrue(a.getLastSeen().isBeforeNow());
        assertFalse(a.getFirstSeen().isAfterNow());
        assertTrue(a.getFirstSeen().isBeforeNow());
        assertNotNull(a.getDocumentationLink());
        assertNotNull(a.getFalsePositives());
        assertNotNull(a.getDescription());

        UnexpectedSSIDBeaconAlert a2 = UnexpectedSSIDBeaconAlert.create(
                "wtf",
                "00:c0:ca:95:68:3b",
                META_NO_WEP,
                buildMockProbe(BANDITS_STANDARD)
        );

        assertTrue(a.sameAs(a2));

        UnexpectedSSIDBeaconAlert a3 = UnexpectedSSIDBeaconAlert.create(
                "wtfDIFF",
                "00:c0:ca:95:68:3b",
                META_NO_WEP,
                buildMockProbe(BANDITS_STANDARD)
        );

        UnexpectedSSIDBeaconAlert a4 = UnexpectedSSIDBeaconAlert.create(
                "wtf",
                "0a:c0:ca:95:68:3b",
                META_NO_WEP,
                buildMockProbe(BANDITS_STANDARD)
        );

        assertFalse(a.sameAs(a3));
        assertFalse(a.sameAs(a4));

        UnexpectedBSSIDProbeRespAlert a6 = UnexpectedBSSIDProbeRespAlert.create(
                "wtf",
                "00:c0:ca:95:68:4b",
                "00:c0:ca:95:68:4b",
                META_NO_WEP,
                buildMockProbe(BANDITS_STANDARD)
        );

        assertFalse(a.sameAs(a6));
    }

    @Test(expectedExceptions = IllegalArgumentException.class)
    public void testAlertHiddenSSID1() {
        UnexpectedSSIDBeaconAlert.create(
                null,
                "00:c0:ca:95:68:3b",
                META_NO_WEP,
                buildMockProbe(BANDITS_STANDARD)
        );
    }

    @Test(expectedExceptions = IllegalArgumentException.class)
    public void testAlertHiddenSSID2() {
        UnexpectedSSIDBeaconAlert.create(
                "",
                "00:c0:ca:95:68:3b",
                META_NO_WEP,
                buildMockProbe(BANDITS_STANDARD)
        );
    }

}