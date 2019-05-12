package horse.wtf.nzyme.dot11.parsers;

import com.codahale.metrics.MetricRegistry;
import horse.wtf.nzyme.dot11.frames.Dot11DeauthenticationFrame;
import org.pcap4j.packet.IllegalRawDataException;
import org.testng.annotations.Test;

import static org.testng.Assert.*;

public class Dot11DeauthenticationFrameParserTest extends FrameParserTest {

    @Test
    public void testDoParse() throws IllegalRawDataException {
        Dot11DeauthenticationFrame frame = new Dot11DeauthenticationFrameParser(new MetricRegistry())
                .doParse(Frames.DEAUTH_1_PAYLOAD, Frames.DEAUTH_1_HEADER, META_NO_WEP);

        assertEquals(frame.transmitter(), "b0:93:5b:1d:c8:f1");
        assertEquals(frame.destination(), "e4:b2:fb:27:50:15");
        assertEquals(frame.bssid(), "b0:93:5b:1d:c8:f1");
        assertEquals((short)frame.reasonCode(), (short)6);
        assertEquals(frame.reasonString(), "Class 2 frame received from nonauthenticated STA");
    }

    @Test
    public void testDoParseAntherReason() throws IllegalRawDataException {
        Dot11DeauthenticationFrame frame = new Dot11DeauthenticationFrameParser(new MetricRegistry())
                .doParse(Frames.DEAUTH_2_PAYLOAD, Frames.DEAUTH_2_HEADER, META_NO_WEP);

        assertEquals(frame.transmitter(), "c2:93:5b:1d:c8:f1");
        assertEquals(frame.destination(), "0c:cb:85:5f:39:16");
        assertEquals(frame.bssid(), "c2:93:5b:1d:c8:f1");
        assertEquals((short)frame.reasonCode(), (short)2);
        assertEquals(frame.reasonString(), "Previous authentication no longer valid");
    }

}