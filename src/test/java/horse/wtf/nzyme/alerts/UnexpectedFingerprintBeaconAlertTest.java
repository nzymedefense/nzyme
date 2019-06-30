package horse.wtf.nzyme.alerts;

import horse.wtf.nzyme.Subsystem;
import org.testng.annotations.Test;

import java.util.ArrayList;
import java.util.List;

import static org.testng.Assert.*;

public class UnexpectedFingerprintBeaconAlertTest extends AlertTest {


    private static final List<String> ONE_BANDIT = new ArrayList<String>(){{
        add("WiFi Pineapple Nano or Tetra (PineAP)");
    }};

    private static final List<String> THREE_BANDITS = new ArrayList<String>(){{
        add("WiFi Pineapple Nano or Tetra (PineAP)");
        add("spacehuhn/esp8266_deauther (attack frames)");
        add("Florida Man");
    }};

    @Test
    public void testAlertStandard() {
        UnexpectedFingerprintBeaconAlert a = UnexpectedFingerprintBeaconAlert.create(
                "wtf",
                ONE_BANDIT,
                "ec398735dc99267d453908d81bfe06ce04cfa2573d0b9edf1d940f0dbf850a9c",
                "00:c0:ca:95:68:3b",
                META_NO_WEP,
                buildMockProbe(BANDITS_STANDARD)
        );

        // Wait a little to make lastSeen() assertions work.
        try {
            Thread.sleep(50);
        } catch (InterruptedException e) { /* noop */ }

        assertEquals(a.getFingerprint(), "ec398735dc99267d453908d81bfe06ce04cfa2573d0b9edf1d940f0dbf850a9c");
        assertEquals(a.getSSID(), "wtf");
        assertEquals(a.getBSSID(), "00:c0:ca:95:68:3b");
        assertEquals(a.getMessage(), "SSID [wtf] was advertised by a device with an unexpected fingerprint. Device type [WiFi Pineapple Nano or Tetra (PineAP)] with fingerprint [ec398735dc99267d453908d81bfe06ce04cfa2573d0b9edf1d940f0dbf850a9c]");
        assertEquals(a.getType(), Alert.Type.UNEXPECTED_FINGERPRINT_BEACON);
        assertEquals(a.getSubsystem(), Subsystem.DOT_11);
        assertEquals(a.getFrameCount(), 1);
        assertFalse(a.getLastSeen().isAfterNow());
        assertTrue(a.getLastSeen().isBeforeNow());
        assertFalse(a.getFirstSeen().isAfterNow());
        assertTrue(a.getFirstSeen().isBeforeNow());
        assertNotNull(a.getDocumentationLink());
        assertNotNull(a.getFalsePositives());
        assertNotNull(a.getDescription());

        UnexpectedFingerprintBeaconAlert a2 = UnexpectedFingerprintBeaconAlert.create(
                "wtf",
                ONE_BANDIT,
                "ec398735dc99267d453908d81bfe06ce04cfa2573d0b9edf1d940f0dbf850a9c",
                "00:c0:ca:95:68:3e",
                META_NO_WEP,
                buildMockProbe(BANDITS_STANDARD)
        );

        assertTrue(a.sameAs(a2));

        UnexpectedFingerprintBeaconAlert a3 = UnexpectedFingerprintBeaconAlert.create(
                "wtfNOTTHESAME",
                ONE_BANDIT,
                "ec398735dc99267d453908d81bfe06ce04cfa2573d0b9edf1d940f0dbf850a9c",
                "00:c0:ca:95:68:3b",
                META_NO_WEP,
                buildMockProbe(BANDITS_STANDARD)
        );

        UnexpectedFingerprintBeaconAlert a4 = UnexpectedFingerprintBeaconAlert.create(
                "wtf",
                ONE_BANDIT,
                "NEIN8735dc99267d453908d81bfe06ce04cfa2573d0b9edf1d940f0dbf850a9c",
                "00:c0:ca:95:68:3b",
                META_NO_WEP,
                buildMockProbe(BANDITS_STANDARD)
        );

        assertFalse(a.sameAs(a3));
        assertFalse(a.sameAs(a4));

        UnexpectedSSIDBeaconAlert a6 = UnexpectedSSIDBeaconAlert.create(
                "wtf",
                "00:c0:ca:95:68:4b",
                META_NO_WEP,
                buildMockProbe(BANDITS_STANDARD)
        );

        assertFalse(a.sameAs(a6));
    }

    @Test
    public void testAlertStandardMultipleBanditNames() {
        UnexpectedFingerprintBeaconAlert a = UnexpectedFingerprintBeaconAlert.create(
                "wtf",
                THREE_BANDITS,
                "ec398735dc99267d453908d81bfe06ce04cfa2573d0b9edf1d940f0dbf850a9c",
                "00:c0:ca:95:68:3b",
                META_NO_WEP,
                buildMockProbe(BANDITS_STANDARD)
        );

        assertEquals(a.getMessage(), "SSID [wtf] was advertised by a device with an unexpected fingerprint. Device type [WiFi Pineapple Nano or Tetra (PineAP),spacehuhn/esp8266_deauther (attack frames),Florida Man] with fingerprint [ec398735dc99267d453908d81bfe06ce04cfa2573d0b9edf1d940f0dbf850a9c]");
        assertEquals(a.getFingerprint(), "ec398735dc99267d453908d81bfe06ce04cfa2573d0b9edf1d940f0dbf850a9c");
    }

    @Test(expectedExceptions = IllegalArgumentException.class)
    public void testAlertHiddenSSID1() {
        UnexpectedFingerprintBeaconAlert.create(
                null,
                ONE_BANDIT,
                "ec398735dc99267d453908d81bfe06ce04cfa2573d0b9edf1d940f0dbf850a9c",
                "00:c0:ca:95:68:3b",
                META_NO_WEP,
                buildMockProbe(BANDITS_STANDARD)
        );
    }

    @Test(expectedExceptions = IllegalArgumentException.class)
    public void testAlertHiddenSSID2() {
        UnexpectedFingerprintBeaconAlert.create(
                "",
                ONE_BANDIT,
                "ec398735dc99267d453908d81bfe06ce04cfa2573d0b9edf1d940f0dbf850a9c",
                "00:c0:ca:95:68:3b",
                META_NO_WEP,
                buildMockProbe(BANDITS_STANDARD)
        ); }

}