package horse.wtf.nzyme.alerts;

import org.joda.time.DateTime;
import org.testng.annotations.Test;

import java.util.UUID;

import static org.testng.Assert.*;

public class AlertTest extends AlertTestHelper {

    @Test
    public void testSetLastSeen() {
        CryptoChangeBeaconAlert a = CryptoChangeBeaconAlert.create(
                "wtf",
                "00:c0:ca:95:68:3b",
                "WPA2-EAM-PSK-CCMP",
                META_NO_WEP,
                buildMockProbe(BANDITS_STANDARD)
        );

        DateTime x = DateTime.now();

        a.setLastSeen(x.minusHours(1));
        assertEquals(a.getLastSeen(), x.minusHours(1));
    }

    @Test
    public void testSetUUID() {
        CryptoChangeBeaconAlert a = CryptoChangeBeaconAlert.create(
                "wtf",
                "00:c0:ca:95:68:3b",
                "WPA2-EAM-PSK-CCMP",
                META_NO_WEP,
                buildMockProbe(BANDITS_STANDARD)
        );

        UUID x = UUID.randomUUID();

        a.setUUID(x);
        assertEquals(a.getUUID(), x);
    }

    @Test
    public void testIncrementFrameCount() {
        CryptoChangeBeaconAlert a = CryptoChangeBeaconAlert.create(
                "wtf",
                "00:c0:ca:95:68:3b",
                "WPA2-EAM-PSK-CCMP",
                META_NO_WEP,
                buildMockProbe(BANDITS_STANDARD)
        );

        assertEquals(a.getFrameCount(), 1);

        a.incrementFrameCount();
        a.incrementFrameCount();

        assertEquals(a.getFrameCount(), 3);
    }

}