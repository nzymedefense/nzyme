package horse.wtf.nzyme.alerts;

import horse.wtf.nzyme.Subsystem;
import org.testng.annotations.Test;

import static org.testng.Assert.*;

public class CryptoChangeProbeRespAlertTest extends AlertTest {


    @Test
    public void testAlertStandard() {
        CryptoChangeProbeRespAlert a = CryptoChangeProbeRespAlert.create(
                "wtf",
                "00:c0:ca:95:68:3b",
                "WPA2-EAM-PSK-CCMP",
                META_NO_WEP,
                buildMockProbe(BANDITS_STANDARD)
        );

        // Wait a little to make lastSeen() assertions work.
        try {
            Thread.sleep(50);
        } catch (InterruptedException e) { /* noop */ }

        assertEquals(a.getEncounteredSecurity(), "WPA2-EAM-PSK-CCMP");
        assertEquals(a.getSSID(), "wtf");
        assertEquals(a.getBSSID(), "00:c0:ca:95:68:3b");
        assertEquals(a.getMessage(), "SSID [wtf] was advertised with unexpected security settings [WPA2-EAM-PSK-CCMP].");
        assertEquals(a.getType(), Alert.Type.CRYPTO_CHANGE_PROBERESP);
        assertEquals(a.getSubsystem(), Subsystem.DOT_11);
        assertEquals(a.getFrameCount(), 1);
        assertFalse(a.getLastSeen().isAfterNow());
        assertTrue(a.getLastSeen().isBeforeNow());
        assertFalse(a.getFirstSeen().isAfterNow());
        assertTrue(a.getFirstSeen().isBeforeNow());
        assertNotNull(a.getDocumentationLink());
        assertNotNull(a.getFalsePositives());
        assertNotNull(a.getDescription());

        CryptoChangeProbeRespAlert a2 = CryptoChangeProbeRespAlert.create(
                "wtf",
                "00:c0:ca:95:68:3b",
                "WPA2-EAM-PSK-CCMP",
                META_NO_WEP,
                buildMockProbe(BANDITS_STANDARD)
        );

        assertTrue(a.sameAs(a2));

        CryptoChangeProbeRespAlert a3 = CryptoChangeProbeRespAlert.create(
                "wtfoooked",
                "00:c0:ca:95:68:3b",
                "WPA2-EAM-PSK-CCMP",
                META_NO_WEP,
                buildMockProbe(BANDITS_STANDARD)
        );

        CryptoChangeProbeRespAlert a4 = CryptoChangeProbeRespAlert.create(
                "wtf",
                "00:c0:ca:95:68:3b",
                "WPA2-EAM-PSK-CCMP-TKIP",
                META_NO_WEP,
                buildMockProbe(BANDITS_STANDARD)
        );

        CryptoChangeProbeRespAlert a5 = CryptoChangeProbeRespAlert.create(
                "wtf",
                "00:c0:ca:95:68:4b",
                "WPA2-EAM-PSK-CCMP",
                META_NO_WEP,
                buildMockProbe(BANDITS_STANDARD)
        );

        assertFalse(a.sameAs(a3));
        assertFalse(a.sameAs(a4));
        assertFalse(a.sameAs(a5));
    }

    @Test(expectedExceptions = IllegalArgumentException.class)
    public void testAlertHiddenSSID1() {
        CryptoChangeProbeRespAlert.create(
                null,
                "00:c0:ca:95:68:3b",
                "WPA2-EAM-PSK-CCMP",
                META_NO_WEP,
                buildMockProbe(BANDITS_STANDARD)
        );
    }


    @Test(expectedExceptions = IllegalArgumentException.class)
    public void testAlertHiddenSSID2() {
        CryptoChangeProbeRespAlert.create(
                "",
                "00:c0:ca:95:68:3b",
                "WPA2-EAM-PSK-CCMP",
                META_NO_WEP,
                buildMockProbe(BANDITS_STANDARD)
        );
    }


}