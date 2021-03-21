package horse.wtf.nzyme.dot11.parsers;

import com.codahale.metrics.MetricRegistry;
import horse.wtf.nzyme.dot11.MalformedFrameException;
import horse.wtf.nzyme.dot11.anonymization.Anonymizer;
import horse.wtf.nzyme.dot11.frames.Dot11AssociationRequestFrame;
import org.pcap4j.packet.IllegalRawDataException;
import org.testng.annotations.Test;

import static org.testng.Assert.*;

public class Dot11AssociationRequestFrameParserTest extends FrameParserTest {

    @Test
    public void testDoParse() throws MalformedFrameException, IllegalRawDataException {
        Dot11AssociationRequestFrame frame = new Dot11AssociationRequestFrameParser(new MetricRegistry(), new Anonymizer(false, ""))
                .doParse(Frames.ASSOC_REQ_1_PAYLOAD, Frames.ASSOC_REQ_1_HEADER, META_NO_WEP);

        assertEquals(frame.ssid(), "ATT4Q5FBC3");
        assertEquals(frame.transmitter(), "ac:81:12:d2:26:7e");
        assertEquals(frame.destination(), "14:ed:bb:79:97:4d");
    }

}