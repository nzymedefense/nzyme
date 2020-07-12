package horse.wtf.nzyme.dot11.parsers;

import com.codahale.metrics.MetricRegistry;
import horse.wtf.nzyme.dot11.MalformedFrameException;
import horse.wtf.nzyme.dot11.anonymization.Anonymizer;
import horse.wtf.nzyme.dot11.frames.Dot11AssociationResponseFrame;
import org.pcap4j.packet.IllegalRawDataException;
import org.testng.annotations.Test;

import static org.testng.Assert.*;

public class Dot11AssociationResponseFrameParserTest extends FrameParserTest {

    @Test
    public void testDoParseSuccessfulAssoc() throws MalformedFrameException, IllegalRawDataException {
        Dot11AssociationResponseFrame frame = new Dot11AssociationResponseFrameParser(new MetricRegistry(), new Anonymizer(false, ""))
                .doParse(Frames.ASSOC_RESP_SUCCESS_1_PAYLOAD, Frames.ASSOC_RESP_SUCCESS_1_HEADER, META_NO_WEP);

        assertEquals(frame.transmitter(), "88:96:4e:4d:77:80");
        assertEquals(frame.destination(), "5c:77:76:d3:26:45");
        assertEquals(frame.response(), "success");
        assertEquals((short)frame.responseCode(), (short)0);
    }

    @Test
    public void testDoParseFailedAssoc() throws MalformedFrameException, IllegalRawDataException {
        Dot11AssociationResponseFrame frame = new Dot11AssociationResponseFrameParser(new MetricRegistry(), new Anonymizer(false, ""))
                .doParse(Frames.ASSOC_RESP_FAILED_1_PAYLOAD, Frames.ASSOC_RESP_FAILED_1_HEADER, META_NO_WEP);

        assertEquals(frame.transmitter(), "88:96:4e:4d:77:80");
        assertEquals(frame.destination(), "5c:77:76:d3:26:45");
        assertEquals(frame.response(), "refused");
        assertEquals((short)frame.responseCode(), (short)1);
    }

}