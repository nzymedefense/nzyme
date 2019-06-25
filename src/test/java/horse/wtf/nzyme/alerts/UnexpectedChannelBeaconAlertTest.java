package horse.wtf.nzyme.alerts;

import horse.wtf.nzyme.Subsystem;
import horse.wtf.nzyme.dot11.Dot11MetaInformation;
import org.testng.annotations.Test;

import static org.testng.Assert.*;

public class UnexpectedChannelBeaconAlertTest extends AlertTest {

    @Test
    public void testAlertStandard() {
        UnexpectedChannelBeaconAlert a = UnexpectedChannelBeaconAlert.create(
                "wtf",
                "00:c0:ca:95:68:3b",
                new Dot11MetaInformation(false, 100, 2400, 1, 0L, false),
                buildMockProbe(BANDITS_STANDARD)
        );

        // Wait a little to make lastSeen() assertions work.
        try {
            Thread.sleep(50);
        } catch (InterruptedException e) { /* noop */ }

        assertEquals(a.getSSID(), "wtf");
        assertEquals(a.getMessage(), "SSID [wtf] was advertised on an unexpected channel.");
        assertEquals(a.getType(), Alert.Type.UNEXPECTED_CHANNEL_BEACON);
        assertEquals(a.getSubsystem(), Subsystem.DOT_11);
        assertEquals(a.getFrameCount(), 1);
        assertFalse(a.getLastSeen().isAfterNow());
        assertTrue(a.getLastSeen().isBeforeNow());
        assertFalse(a.getFirstSeen().isAfterNow());
        assertTrue(a.getFirstSeen().isBeforeNow());
        assertNotNull(a.getDocumentationLink());
        assertNotNull(a.getFalsePositives());
        assertNotNull(a.getDescription());

        UnexpectedChannelBeaconAlert a2 = UnexpectedChannelBeaconAlert.create(
                "wtf",
                "00:c0:ca:95:68:3e",
                new Dot11MetaInformation(false, 100, 2400, 1, 0L, false),
                buildMockProbe(BANDITS_STANDARD)
        );

        assertTrue(a.sameAs(a2));

        UnexpectedChannelBeaconAlert a3 = UnexpectedChannelBeaconAlert.create(
                "wtfDIFF",
                "00:c0:ca:95:68:3b",
                new Dot11MetaInformation(false, 100, 2400, 1, 0L, false),
                buildMockProbe(BANDITS_STANDARD)
        );

        UnexpectedChannelBeaconAlert a4 = UnexpectedChannelBeaconAlert.create(
                "wtf",
                "00:c0:ca:95:68:3b",
                new Dot11MetaInformation(false, 100, 2400, 6, 0L, false),
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
        UnexpectedChannelBeaconAlert.create(
                null,
                "00:c0:ca:95:68:3b",
                META_NO_WEP,
                buildMockProbe(BANDITS_STANDARD)
        );
    }

    @Test(expectedExceptions = IllegalArgumentException.class)
    public void testAlertHiddenSSID2() {
        UnexpectedChannelBeaconAlert.create(
                "",
                "00:c0:ca:95:68:3b",
                META_NO_WEP,
                buildMockProbe(BANDITS_STANDARD)
        );
    }

}