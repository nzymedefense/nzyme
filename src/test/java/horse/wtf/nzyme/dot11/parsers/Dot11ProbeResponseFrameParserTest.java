package horse.wtf.nzyme.dot11.parsers;

import com.codahale.metrics.MetricRegistry;
import horse.wtf.nzyme.dot11.MalformedFrameException;
import horse.wtf.nzyme.dot11.frames.Dot11ProbeResponseFrame;
import org.pcap4j.packet.IllegalRawDataException;
import org.testng.annotations.Test;

import static org.testng.Assert.*;

public class Dot11ProbeResponseFrameParserTest extends FrameParserTest {

    @Test
    public void testDoParse() throws MalformedFrameException, IllegalRawDataException {
        Dot11ProbeResponseFrame frame = new Dot11ProbeResponseFrameParser(new MetricRegistry())
                .doParse(Frames.PROBE_RESP_1_PAYLOAD, Frames.PROBE_RESP_1_HEADER, META_NO_WEP);

        assertEquals(frame.ssid(), "Home 5F48");
        assertEquals(frame.destination(), "3c:8d:20:52:e4:87");
        assertEquals(frame.transmitter(), "b0:93:5b:1d:c8:f1");
        assertEquals(frame.transmitterFingerprint(), "2187f729bf5093a1347acac583c86a1ed72c5ce2df0ed2628cafcee4e78e591d");
        assertEquals(frame.taggedParameters().isWPS(), true);
        assertEquals(frame.taggedParameters().getFullSecurityString(), "WPA2-PSK-CCMP");
    }

    @Test
    public void testDoParseNoSecurity() throws MalformedFrameException, IllegalRawDataException {
        Dot11ProbeResponseFrame frame = new Dot11ProbeResponseFrameParser(new MetricRegistry())
                .doParse(Frames.PROBE_RESP_NO_SECURITY_1_PAYLOAD, Frames.PROBE_RESP_NO_SECURITY_1_HEADER, META_NO_WEP);

        assertEquals(frame.ssid(), "xfinitywifi");
        assertEquals(frame.destination(), "3c:8d:20:52:e4:87");
        assertEquals(frame.transmitter(), "c2:93:5b:1d:c8:f1");
        assertEquals(frame.transmitterFingerprint(), "0fccc2740091c4a668b8b0f1e7a7ad4e93d62637b8cfb6a4cbb678b1d37477a6");
        assertEquals(frame.taggedParameters().isWPS(), false);
        assertEquals(frame.taggedParameters().getFullSecurityString(), "NONE");
    }

}