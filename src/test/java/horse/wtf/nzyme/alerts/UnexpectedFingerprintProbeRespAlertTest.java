package horse.wtf.nzyme.alerts;

import horse.wtf.nzyme.Subsystem;
import org.testng.annotations.Test;

import java.util.ArrayList;
import java.util.List;

import static org.testng.Assert.*;

public class UnexpectedFingerprintProbeRespAlertTest extends AlertTest {
    @Test
    public void testAlertStandard() {
        UnexpectedFingerprintProbeRespAlert a = UnexpectedFingerprintProbeRespAlert.create(
                "wtf",
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
        assertEquals(a.getMessage(), "SSID [wtf] was advertised with a probe response by a device with unexpected fingerprint [ec398735dc99267d453908d81bfe06ce04cfa2573d0b9edf1d940f0dbf850a9c]");
        assertEquals(a.getType(), Alert.Type.UNEXPECTED_FINGERPRINT_PROBERESP);
        assertEquals(a.getSubsystem(), Subsystem.DOT_11);
        assertEquals(a.getFrameCount(), 1);
        assertFalse(a.getLastSeen().isAfterNow());
        assertTrue(a.getLastSeen().isBeforeNow());
        assertFalse(a.getFirstSeen().isAfterNow());
        assertTrue(a.getFirstSeen().isBeforeNow());
        assertNotNull(a.getDocumentationLink());
        assertNotNull(a.getFalsePositives());
        assertNotNull(a.getDescription());

        UnexpectedFingerprintProbeRespAlert a2 = UnexpectedFingerprintProbeRespAlert.create(
                "wtf",
                "ec398735dc99267d453908d81bfe06ce04cfa2573d0b9edf1d940f0dbf850a9c",
                "00:c0:ca:95:68:3e",
                META_NO_WEP,
                buildMockProbe(BANDITS_STANDARD)
        );

        assertTrue(a.sameAs(a2));

        UnexpectedFingerprintProbeRespAlert a3 = UnexpectedFingerprintProbeRespAlert.create(
                "wtfNOTTHESAME",
                "ec398735dc99267d453908d81bfe06ce04cfa2573d0b9edf1d940f0dbf850a9c",
                "00:c0:ca:95:68:3b",
                META_NO_WEP,
                buildMockProbe(BANDITS_STANDARD)
        );

        UnexpectedFingerprintProbeRespAlert a4 = UnexpectedFingerprintProbeRespAlert.create(
                "wtf",
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

    @Test(expectedExceptions = IllegalArgumentException.class)
    public void testAlertHiddenSSID1() {
        UnexpectedFingerprintProbeRespAlert.create(
                null,
                "ec398735dc99267d453908d81bfe06ce04cfa2573d0b9edf1d940f0dbf850a9c",
                "00:c0:ca:95:68:3b",
                META_NO_WEP,
                buildMockProbe(BANDITS_STANDARD)
        );
    }

    @Test(expectedExceptions = IllegalArgumentException.class)
    public void testAlertHiddenSSID2() {
        UnexpectedFingerprintProbeRespAlert.create(
                "",
                "ec398735dc99267d453908d81bfe06ce04cfa2573d0b9edf1d940f0dbf850a9c",
                "00:c0:ca:95:68:3b",
                META_NO_WEP,
                buildMockProbe(BANDITS_STANDARD)
        );
    }

    @Test(expectedExceptions = IllegalArgumentException.class)
    public void testAlertEmptyFingerprint1() {
        UnexpectedFingerprintProbeRespAlert.create(
                "foo",
                null,
                "00:c0:ca:95:68:3b",
                META_NO_WEP,
                buildMockProbe(BANDITS_STANDARD)
        );
    }

    @Test(expectedExceptions = IllegalArgumentException.class)
    public void testAlertEmptyFingerprint2() {
        UnexpectedFingerprintProbeRespAlert.create(
                "foo",
                "",
                "00:c0:ca:95:68:3b",
                META_NO_WEP,
                buildMockProbe(BANDITS_STANDARD)
        );
    }

}