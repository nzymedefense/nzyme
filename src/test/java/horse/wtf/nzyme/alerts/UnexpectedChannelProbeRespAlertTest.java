package horse.wtf.nzyme.alerts;

import horse.wtf.nzyme.Subsystem;
import horse.wtf.nzyme.dot11.Dot11MetaInformation;
import org.testng.annotations.Test;

import static org.testng.Assert.*;

public class UnexpectedChannelProbeRespAlertTest extends AlertTest {

    @Test
    public void testAlertStandard() {
        UnexpectedChannelProbeRespAlert a = UnexpectedChannelProbeRespAlert.create(
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
        assertEquals(a.getMessage(), "SSID [wtf] was advertised with a probe response frame on an unexpected channel.");
        assertEquals(a.getType(), Alert.Type.UNEXPECTED_CHANNEL_PROBERESP);
        assertEquals(a.getSubsystem(), Subsystem.DOT_11);
        assertEquals(a.getFrameCount(), 1);
        assertFalse(a.getLastSeen().isAfterNow());
        assertTrue(a.getLastSeen().isBeforeNow());
        assertFalse(a.getFirstSeen().isAfterNow());
        assertTrue(a.getFirstSeen().isBeforeNow());
        assertNotNull(a.getDocumentationLink());
        assertNotNull(a.getFalsePositives());
        assertNotNull(a.getDescription());

        UnexpectedChannelProbeRespAlert a2 = UnexpectedChannelProbeRespAlert.create(
                "wtf",
                "00:c0:ca:95:68:3e",
                new Dot11MetaInformation(false, 100, 2400, 1, 0L, false),
                buildMockProbe(BANDITS_STANDARD)
        );

        assertTrue(a.sameAs(a2));

        UnexpectedChannelProbeRespAlert a3 = UnexpectedChannelProbeRespAlert.create(
                "wtfDIFF",
                "00:c0:ca:95:68:3b",
                new Dot11MetaInformation(false, 100, 2400, 1, 0L, false),
                buildMockProbe(BANDITS_STANDARD)
        );

        UnexpectedChannelProbeRespAlert a4 = UnexpectedChannelProbeRespAlert.create(
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
        UnexpectedChannelProbeRespAlert.create(
                null,
                "00:c0:ca:95:68:3b",
                META_NO_WEP,
                buildMockProbe(BANDITS_STANDARD)
        );
    }

    @Test(expectedExceptions = IllegalArgumentException.class)
    public void testAlertHiddenSSID2() {
        UnexpectedChannelProbeRespAlert.create(
                "",
                "00:c0:ca:95:68:3b",
                META_NO_WEP,
                buildMockProbe(BANDITS_STANDARD)
        );
    }

}