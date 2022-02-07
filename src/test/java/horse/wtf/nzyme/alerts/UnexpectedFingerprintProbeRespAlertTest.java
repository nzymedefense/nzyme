package horse.wtf.nzyme.alerts;

import horse.wtf.nzyme.Subsystem;
import org.joda.time.DateTime;
import org.testng.annotations.Test;

import static org.testng.Assert.*;

public class UnexpectedFingerprintProbeRespAlertTest extends AlertTest {
    @Test
    public void testAlertStandard() {
        UnexpectedFingerprintProbeRespAlert a = UnexpectedFingerprintProbeRespAlert.create(
                DateTime.now(),
                "wtf",
                "ec398735dc99267d453908d81bfe06ce04cfa2573d0b9edf1d940f0dbf850a9c",
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

        assertEquals(a.getFingerprint(), "ec398735dc99267d453908d81bfe06ce04cfa2573d0b9edf1d940f0dbf850a9c");
        assertEquals(a.getSSID(), "wtf");
        assertEquals(a.getBSSID(), "00:c0:ca:95:68:3b");
        assertEquals(a.getMessage(), "SSID [wtf] was advertised with a probe response by a device with unexpected fingerprint [ec398735dc99267d453908d81bfe06ce04cfa2573d0b9edf1d940f0dbf850a9c]");
        assertEquals(a.getType(), Alert.TYPE.UNEXPECTED_FINGERPRINT_PROBERESP);
        assertEquals(a.getSubsystem(), Subsystem.DOT_11);
        assertEquals(a.getFrameCount(), (Long) 1L);
        assertFalse(a.getLastSeen().isAfterNow());
        assertTrue(a.getLastSeen().isBeforeNow());
        assertFalse(a.getFirstSeen().isAfterNow());
        assertTrue(a.getFirstSeen().isBeforeNow());
        assertNotNull(a.getDocumentationLink());
        assertNotNull(a.getFalsePositives());
        assertNotNull(a.getDescription());

        UnexpectedFingerprintProbeRespAlert a2 = UnexpectedFingerprintProbeRespAlert.create(
                DateTime.now(),
                "wtf",
                "ec398735dc99267d453908d81bfe06ce04cfa2573d0b9edf1d940f0dbf850a9c",
                "00:c0:ca:95:68:3b",
                1,
                1000,
                -50,
                1
        );

        assertTrue(a.sameAs(a2));

        UnexpectedFingerprintProbeRespAlert a3 = UnexpectedFingerprintProbeRespAlert.create(
                DateTime.now(),
                "wtfNOTTHESAME",
                "ec398735dc99267d453908d81bfe06ce04cfa2573d0b9edf1d940f0dbf850a9c",
                "00:c0:ca:95:68:3b",
                1,
                1000,
                -50,
                1
        );

        UnexpectedFingerprintProbeRespAlert a4 = UnexpectedFingerprintProbeRespAlert.create(
                DateTime.now(),
                "wtf",
                "NEIN8735dc99267d453908d81bfe06ce04cfa2573d0b9edf1d940f0dbf850a9c",
                "00:c0:ca:95:68:3b",
                1,
                1000,
                -50,
                1
        );

        assertFalse(a.sameAs(a3));
        assertFalse(a.sameAs(a4));

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

        UnexpectedFingerprintProbeRespAlert a7 = UnexpectedFingerprintProbeRespAlert.create(
                DateTime.now(),
                "wtf",
                "ec398735dc99267d453908d81bfe06ce04cfa2573d0b9edf1d940f0dbf850a9c",
                "00:c0:ca:95:68:00",
                1,
                1000,
                -50,
                1
        );

        assertFalse(a.sameAs(a7));
    }

    @Test(expectedExceptions = IllegalArgumentException.class)
    public void testAlertHiddenSSID1() {
        UnexpectedFingerprintProbeRespAlert.create(
                DateTime.now(),
                null,
                "ec398735dc99267d453908d81bfe06ce04cfa2573d0b9edf1d940f0dbf850a9c",
                "00:c0:ca:95:68:3b",
                1,
                1000,
                -50,
                1
        );
    }

    @Test(expectedExceptions = IllegalArgumentException.class)
    public void testAlertHiddenSSID2() {
        UnexpectedFingerprintProbeRespAlert.create(
                DateTime.now(),
                "",
                "ec398735dc99267d453908d81bfe06ce04cfa2573d0b9edf1d940f0dbf850a9c",
                "00:c0:ca:95:68:3b",
                1,
                1000,
                -50,
                1
        );
    }

    @Test(expectedExceptions = IllegalArgumentException.class)
    public void testAlertEmptyFingerprint1() {
        UnexpectedFingerprintProbeRespAlert.create(
                DateTime.now(),
                "foo",
                null,
                "00:c0:ca:95:68:3b",
                1,
                1000,
                -50,
                1
        );
    }

    @Test(expectedExceptions = IllegalArgumentException.class)
    public void testAlertEmptyFingerprint2() {
        UnexpectedFingerprintProbeRespAlert.create(
                DateTime.now(),
                "foo",
                "",
                "00:c0:ca:95:68:3b",
                1,
                1000,
                -50,
                1
        );
    }

}