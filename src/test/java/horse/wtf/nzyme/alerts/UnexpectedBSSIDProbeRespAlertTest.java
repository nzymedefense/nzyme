package horse.wtf.nzyme.alerts;

import horse.wtf.nzyme.Subsystem;
import org.testng.annotations.Test;

import static org.testng.Assert.*;

public class UnexpectedBSSIDProbeRespAlertTest extends AlertTestHelper {

    @Test
    public void testAlertStandard() {
        UnexpectedBSSIDProbeRespAlert a = UnexpectedBSSIDProbeRespAlert.create(
                "wtf",
                "00:c0:ca:95:68:3b",
                "e0:22:03:f8:a3:39",
                META_NO_WEP,
                buildMockProbe(BANDITS_STANDARD)
        );

        // Wait a little to make lastSeen() assertions work.
        try {
            Thread.sleep(50);
        } catch (InterruptedException e) { /* noop */ }

        assertEquals(a.getSSID(), "wtf");
        assertEquals(a.getBSSID(), "00:c0:ca:95:68:3b");
        assertEquals(a.getDestination(), "e0:22:03:f8:a3:39");
        assertEquals(a.getMessage(), "SSID [wtf] was advertised with probe response frame by unexpected BSSID [00:c0:ca:95:68:3b] for [e0:22:03:f8:a3:39]");
        assertEquals(a.getType(), Alert.Type.UNEXPECTED_BSSID_PROBERESP);
        assertEquals(a.getSubsystem(), Subsystem.DOT_11);
        assertEquals(a.getFrameCount(), (Long) 1L);
        assertFalse(a.getLastSeen().isAfterNow());
        assertTrue(a.getLastSeen().isBeforeNow());
        assertFalse(a.getFirstSeen().isAfterNow());
        assertTrue(a.getFirstSeen().isBeforeNow());
        assertNotNull(a.getDocumentationLink());
        assertNotNull(a.getFalsePositives());
        assertNotNull(a.getDescription());

        UnexpectedBSSIDProbeRespAlert a2 = UnexpectedBSSIDProbeRespAlert.create(
                "wtf",
                "00:c0:ca:95:68:3b",
                "aa:22:03:f8:a3:39",
                META_NO_WEP,
                buildMockProbe(BANDITS_STANDARD)
        );

        assertTrue(a.sameAs(a2));

        UnexpectedBSSIDProbeRespAlert a3 = UnexpectedBSSIDProbeRespAlert.create(
                "wtfNOTTHESAME",
                "00:c0:ca:95:68:3b",
                "e0:22:03:f8:a3:39",
                META_NO_WEP,
                buildMockProbe(BANDITS_STANDARD)
        );

        UnexpectedBSSIDProbeRespAlert a4 = UnexpectedBSSIDProbeRespAlert.create(
                "wtf",
                "00:c0:ca:95:68:3e",
                "e0:22:03:f8:a3:39",
                META_NO_WEP,
                buildMockProbe(BANDITS_STANDARD)
        );

        assertFalse(a.sameAs(a3));
        assertFalse(a.sameAs(a4));

        UnexpectedChannelBeaconAlert a6 = UnexpectedChannelBeaconAlert.create(
                "wtf",
                "00:c0:ca:95:68:4b",
                META_NO_WEP,
                buildMockProbe(BANDITS_STANDARD)
        );

        assertFalse(a.sameAs(a6));
    }

    @Test(expectedExceptions = IllegalArgumentException.class)
    public void testAlertHiddenSSID1() {
        UnexpectedBSSIDProbeRespAlert.create(
                null,
                "00:c0:ca:95:68:3b",
                "e0:22:03:f8:a3:39",
                META_NO_WEP,
                buildMockProbe(BANDITS_STANDARD)
        );
    }

    @Test(expectedExceptions = IllegalArgumentException.class)
    public void testAlertHiddenSSID2() {
        UnexpectedBSSIDProbeRespAlert.create(
                "",
                "00:c0:ca:95:68:3b",
                "e0:22:03:f8:a3:39",
                META_NO_WEP,
                buildMockProbe(BANDITS_STANDARD)
        );
    }

}