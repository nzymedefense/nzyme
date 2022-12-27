package app.nzyme.core.alerts;

import org.joda.time.DateTime;
import org.testng.annotations.Test;

import java.util.UUID;

import static org.testng.Assert.*;

public class AlertTest extends AlertTestHelper {

    @Test
    public void testSetLastSeen() {
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

        DateTime x = DateTime.now();

        a.setLastSeen(x.minusHours(1));
        assertEquals(a.getLastSeen(), x.minusHours(1));
    }

    @Test
    public void testSetUUID() {
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

        UUID x = UUID.randomUUID();

        a.setUUID(x);
        assertEquals(a.getUUID(), x);
    }

    @Test
    public void testIncrementFrameCount() {
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

        assertEquals(a.getFrameCount(), (Long) 1L);

        a.incrementFrameCount();
        a.incrementFrameCount();

        assertEquals(a.getFrameCount(),  (Long) 3L);
    }

}