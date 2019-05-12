package horse.wtf.nzyme.dot11.parsers;

import com.codahale.metrics.MetricRegistry;
import horse.wtf.nzyme.dot11.MalformedFrameException;
import horse.wtf.nzyme.dot11.frames.Dot11AuthenticationFrame;
import org.pcap4j.packet.IllegalRawDataException;
import org.testng.annotations.Test;

import static org.testng.Assert.*;

public class Dot11AuthenticationFrameParserTest extends FrameParserTest {

    @Test
    public void testDoParseSuccessfulAuth() throws MalformedFrameException, IllegalRawDataException {
        Dot11AuthenticationFrame frame = new Dot11AuthenticationFrameParser(new MetricRegistry())
                .doParse(Frames.AUTH_SUCCESS_1_PAYLOAD, Frames.AUTH_SUCCESS_1_HEADER, META_NO_WEP);

        assertEquals(frame.transmitter(), "ac:5f:3e:b9:5d:be");
        assertEquals(frame.destination(), "e0:22:03:f8:a3:39");
        assertEquals(frame.statusString(), "success");
        assertEquals((short) frame.statusCode(), (short) 0);
        assertEquals((short) frame.transactionSequence(), (short) 1);
    }

    @Test
    public void testDoParseFailedAuth() throws MalformedFrameException, IllegalRawDataException {
        Dot11AuthenticationFrame frame = new Dot11AuthenticationFrameParser(new MetricRegistry())
                .doParse(Frames.AUTH_FAILED_1_PAYLOAD, Frames.AUTH_FAILED_1_HEADER, META_NO_WEP);

        assertEquals(frame.transmitter(), "ac:5f:3e:b9:5d:be");
        assertEquals(frame.destination(), "e0:22:03:f8:a3:39");
        assertEquals(frame.statusString(), "failure");
        assertEquals((short) frame.statusCode(), (short) 1);
        assertEquals((short) frame.transactionSequence(), (short) 1);
    }

    @Test
    public void testDoParseInvalidResponse() throws MalformedFrameException, IllegalRawDataException {
        Dot11AuthenticationFrame frame = new Dot11AuthenticationFrameParser(new MetricRegistry())
                .doParse(Frames.AUTH_INVALID_RESPONSE_1_PAYLOAD, Frames.AUTH_INVALID_RESPONSE_1_HEADER, META_NO_WEP);

        assertEquals(frame.transmitter(), "ac:5f:3e:b9:5d:be");
        assertEquals(frame.destination(), "e0:22:03:f8:a3:39");
        assertEquals(frame.statusString(), "Invalid/Unknown (5)");
        assertEquals((short) frame.statusCode(), (short) 5);
        assertEquals((short) frame.transactionSequence(), (short) 1);
    }

}