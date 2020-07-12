package horse.wtf.nzyme.dot11.parsers;

import com.codahale.metrics.MetricRegistry;
import horse.wtf.nzyme.dot11.MalformedFrameException;
import horse.wtf.nzyme.dot11.anonymization.Anonymizer;
import horse.wtf.nzyme.dot11.frames.Dot11ProbeRequestFrame;
import org.pcap4j.packet.IllegalRawDataException;
import org.testng.annotations.Test;

import static org.testng.Assert.*;

public class Dot11ProbeRequestFrameParserTest extends FrameParserTest {

    @Test
    public void testDoParse() throws MalformedFrameException, IllegalRawDataException {
        Dot11ProbeRequestFrame frame = new Dot11ProbeRequestFrameParser(new MetricRegistry(), new Anonymizer(false, ""))
                .doParse(Frames.PROBE_REQ_1_PAYLOAD, Frames.PROBE_REQ_1_HEADER, META_NO_WEP);

        assertEquals(frame.ssid(), "ATT6r8YXW9");
        assertEquals(frame.requester(), "3c:8d:20:25:20:e9");
        assertFalse(frame.isBroadcastProbe());
    }

    @Test
    public void testDoParseAnotherFrame() throws MalformedFrameException, IllegalRawDataException {
        Dot11ProbeRequestFrame frame = new Dot11ProbeRequestFrameParser(new MetricRegistry(), new Anonymizer(false, ""))
                .doParse(Frames.PROBE_REQ_2_PAYLOAD, Frames.PROBE_REQ_2_HEADER, META_NO_WEP);

        assertEquals(frame.ssid(), "TMobileWingman");
        assertEquals(frame.requester(), "a8:51:5b:7f:1b:2d");
        assertFalse(frame.isBroadcastProbe());
    }

    @Test
    public void testDoParseBroadcast() throws MalformedFrameException, IllegalRawDataException {
        Dot11ProbeRequestFrame frame = new Dot11ProbeRequestFrameParser(new MetricRegistry(), new Anonymizer(false, ""))
                .doParse(Frames.PROBE_REQ_BROADCAST_1_PAYLOAD, Frames.PROBE_REQ_BROADCAST_1_HEADER, META_NO_WEP);

        assertNull(frame.ssid());
        assertEquals(frame.requester(), "f8:da:0c:2e:af:1c");
        assertTrue(frame.isBroadcastProbe());
    }

}