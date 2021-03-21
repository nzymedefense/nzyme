package horse.wtf.nzyme.alerts;

import horse.wtf.nzyme.Subsystem;
import org.joda.time.DateTime;
import org.testng.annotations.Test;

import static org.testng.Assert.*;

public class CryptoChangeBeaconAlertTest extends AlertTestHelper {

    @Test
    public void testAlertStandard() {
        CryptoChangeBeaconAlert a = CryptoChangeBeaconAlert.create(
                DateTime.now(),
                "wtf",
                "00:c0:ca:95:68:3b",
                "WPA2-EAM-PSK-CCMP",
                1,
                1000,
                -50,
                1
        );

        // Wait a little to make lastSeen() assertions work.
        try {
            Thread.sleep(50);
        } catch (InterruptedException e) { /* noop */ }

        assertEquals(a.getEncounteredSecurity(), "WPA2-EAM-PSK-CCMP");
        assertEquals(a.getSSID(), "wtf");
        assertEquals(a.getBSSID(), "00:c0:ca:95:68:3b");
        assertEquals(a.getMessage(), "SSID [wtf] was advertised with unexpected security settings [WPA2-EAM-PSK-CCMP].");
        assertEquals(a.getType(), Alert.TYPE.CRYPTO_CHANGE_BEACON);
        assertEquals(a.getSubsystem(), Subsystem.DOT_11);
        assertEquals(a.getFrameCount(), (Long) 1L);
        assertFalse(a.getLastSeen().isAfterNow());
        assertTrue(a.getLastSeen().isBeforeNow());
        assertFalse(a.getFirstSeen().isAfterNow());
        assertTrue(a.getFirstSeen().isBeforeNow());
        assertNotNull(a.getDocumentationLink());
        assertNotNull(a.getFalsePositives());
        assertNotNull(a.getDescription());

        CryptoChangeBeaconAlert a2 = CryptoChangeBeaconAlert.create(
                DateTime.now(),
                "wtf",
                "00:c0:ca:95:68:3b",
                "WPA2-EAM-PSK-CCMP",
                1,
                1000,
                -50,
                1
        );

        assertTrue(a.sameAs(a2));

        CryptoChangeBeaconAlert a3 = CryptoChangeBeaconAlert.create(
                DateTime.now(),
                "wtfoooked",
                "00:c0:ca:95:68:3b",
                "WPA2-EAM-PSK-CCMP",
                1,
                1000,
                -50,
                1
        );

        CryptoChangeBeaconAlert a4 = CryptoChangeBeaconAlert.create(
                DateTime.now(),
                "wtf",
                "00:c0:ca:95:68:3b",
                "WPA2-EAM-PSK-CCMP-TKIP",
                1,
                1000,
                -50,
                1
        );

        CryptoChangeBeaconAlert a5 = CryptoChangeBeaconAlert.create(
                DateTime.now(),
                "wtf",
                "00:c0:ca:95:68:4b",
                "WPA2-EAM-PSK-CCMP",
                1,
                1000,
                -50,
                1
        );

        assertFalse(a.sameAs(a3));
        assertFalse(a.sameAs(a4));
        assertFalse(a.sameAs(a5));

        UnexpectedSSIDBeaconAlert a6 = UnexpectedSSIDBeaconAlert.create(
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
        CryptoChangeBeaconAlert.create(
                DateTime.now(),
                null,
                "00:c0:ca:95:68:3b",
                "WPA2-EAM-PSK-CCMP",
                1,
                1000,
                -50,
                1
        );
    }


    @Test(expectedExceptions = IllegalArgumentException.class)
    public void testAlertHiddenSSID2() {
        CryptoChangeBeaconAlert.create(
                DateTime.now(),
                "",
                "00:c0:ca:95:68:3b",
                "WPA2-EAM-PSK-CCMP",
                1,
                1000,
                -50,
                1
        );
    }

}