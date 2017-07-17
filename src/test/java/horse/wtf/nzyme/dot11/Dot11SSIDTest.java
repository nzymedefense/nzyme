package horse.wtf.nzyme.dot11;

import horse.wtf.nzyme.handlers.ProbeResponseFrameHandler;
import org.testng.annotations.Test;

import static org.testng.Assert.*;

public class Dot11SSIDTest {

    private static final byte[] PROBE_RESP = new byte[]{80, 0, 60, 0, -54, 120, -13, 90, -43, -23, 0, 36, -88, -123, 22,
            1, 0, 36, -88, -123, 22, 1, -16, 102, 111, -37, 43, -83, 6, 0, 0, 0, 100, 0, 1, 8, 0, 12, 85, 110, 105, 116,
            101, 100, 95, 87, 105, 45, 70, 105, 1, 8, -116, 18, -104, 36, -80, 72, 96, 108, 3, 1, 36, 7, 66, 67, 65, 32,
            36, 1, 23, 40, 1, 23, 44, 1, 23, 48, 1, 23, 52, 1, 23, 56, 1, 23, 60, 1, 23, 64, 1, 23, 100, 1, 23, 104, 1,
            23, 108, 1, 23, 112, 1, 23, 116, 1, 23, -124, 1, 23, -120, 1, 23, -116, 1, 23, -107, 1, 30, -103, 1, 30, -99,
            1, 30, -95, 1, 30, -91, 1, 30, -35, 24, 0, 80, -14, 2, 1, 1, -124, 0, 3, -92, 0, 0, 39, -92, 0, 0, 66, 67,
            94, 0, 98, 50, 47, 0, -35, 30, 0, -112, 76, 51, 12, 0, 31, -1, -1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0,
            0, 0, 0, 0, 0, 0, 0, 45, 26, 12, 0, 31, -1, -1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0,
            0, -35, 26, 0, -112, 76, 52, 36, 0, 8, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 61, 22, 36,
            0, 8, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 82, 0, -6, -123};

    @Test
    public void testExtractSSID() throws Exception {
        String ssid = null;
        try {
            ssid = Dot11SSID.extractSSID(
                    ProbeResponseFrameHandler.SSID_LENGTH_POSITION,
                    ProbeResponseFrameHandler.SSID_POSITION,
                    PROBE_RESP
            );
        } catch (MalformedFrameException e) {
            fail("malformed frame");
        }

        assertEquals(ssid, "United_Wi-Fi");
    }

}