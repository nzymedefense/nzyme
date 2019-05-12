package horse.wtf.nzyme.dot11.parsers;

import com.codahale.metrics.MetricRegistry;
import horse.wtf.nzyme.dot11.frames.Dot11DisassociationFrame;
import org.pcap4j.packet.IllegalRawDataException;
import org.testng.annotations.Test;

import static org.testng.Assert.*;

public class Dot11DisassociationFrameParserTest extends FrameParserTest {

    @Test
    public void testDoParse() throws IllegalRawDataException {
        Dot11DisassociationFrame frame = new Dot11DisassociationFrameParser(new MetricRegistry())
                .doParse(Frames.DISASSOC_1_PAYLOAD, Frames.DISASSOC_1_HEADER, META_NO_WEP);

        assertEquals(frame.transmitter(), "b4:fb:e4:41:f6:45");
        assertEquals(frame.destination(), "b0:70:2d:56:1c:f7");
        assertEquals((short)frame.reasonCode(), (short)8);
        assertEquals(frame.reasonString(), "Disassociated because sending STA is leaving (or has left) BSS");
    }

    @Test
    public void testDoParseAnotherFrame() throws IllegalRawDataException {
        Dot11DisassociationFrame frame = new Dot11DisassociationFrameParser(new MetricRegistry())
                .doParse(Frames.DISASSOC_2_PAYLOAD, Frames.DISASSOC_2_HEADER, META_NO_WEP);

        assertEquals(frame.transmitter(), "0c:54:a5:99:06:58");
        assertEquals(frame.destination(), "60:14:b3:51:fe:6b");
        assertEquals((short)frame.reasonCode(), (short)2);
        assertEquals(frame.reasonString(), "Previous authentication no longer valid");
    }

}