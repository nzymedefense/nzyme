package horse.wtf.nzyme.dot11.parsers;

import com.codahale.metrics.MetricRegistry;
import horse.wtf.nzyme.dot11.MalformedFrameException;
import horse.wtf.nzyme.dot11.frames.Dot11AuthenticationFrame;
import org.pcap4j.packet.IllegalRawDataException;
import org.testng.annotations.Test;

import static org.testng.Assert.*;

public class Dot11AuthenticationFrameParserTest extends FrameParserTest {

    @Test
    public void testDoParseSuccessfulAuthStage1() throws MalformedFrameException, IllegalRawDataException {
        Dot11AuthenticationFrame frame = new Dot11AuthenticationFrameParser(new MetricRegistry())
                .doParse(Frames.AUTH_SUCCESS_STAGE_1_PAYLOAD, Frames.AUTH_SUCCESS_STAGE_1_HEADER, META_NO_WEP);

        assertEquals(frame.algorithm(), Dot11AuthenticationFrameParser.ALGORITHM_TYPE.OPEN_SYSTEM);
        assertEquals(frame.transmitter(), "ac:5f:3e:b9:5d:be");
        assertEquals(frame.destination(), "e0:22:03:f8:a3:39");
        assertEquals(frame.statusString(), "success");
        assertEquals((short) frame.statusCode(), (short) 0);
        assertEquals((short) frame.transactionSequence(), (short) 1);
    }

    @Test
    public void testDoParseSuccessfulAuthStage2() throws MalformedFrameException, IllegalRawDataException {
        Dot11AuthenticationFrame frame = new Dot11AuthenticationFrameParser(new MetricRegistry())
                .doParse(Frames.AUTH_SUCCESS_STAGE_2_PAYLOAD, Frames.AUTH_SUCCESS_STAGE_2_HEADER, META_NO_WEP);

        assertEquals(frame.algorithm(), Dot11AuthenticationFrameParser.ALGORITHM_TYPE.OPEN_SYSTEM);
        assertEquals(frame.transmitter(), "2c:5d:93:04:5c:09");
        assertEquals(frame.destination(), "64:76:ba:d8:5d:ab");
        assertEquals(frame.statusString(), "success");
        assertEquals((short) frame.statusCode(), (short) 0);
        assertEquals((short) frame.transactionSequence(), (short) 2);
    }

    @Test
    public void testDoParseFailedAuth() throws MalformedFrameException, IllegalRawDataException {
        Dot11AuthenticationFrame frame = new Dot11AuthenticationFrameParser(new MetricRegistry())
                .doParse(Frames.AUTH_FAILED_STAGE_1_PAYLOAD, Frames.AUTH_FAILED_STAGE_1_HEADER, META_NO_WEP);

        assertEquals(frame.algorithm(), Dot11AuthenticationFrameParser.ALGORITHM_TYPE.OPEN_SYSTEM);
        assertEquals(frame.transmitter(), "ac:5f:3e:b9:5d:be");
        assertEquals(frame.destination(), "e0:22:03:f8:a3:39");
        assertEquals(frame.statusString(), "failure");
        assertEquals((short) frame.statusCode(), (short) 1);
        assertEquals((short) frame.transactionSequence(), (short) 1);
    }

    @Test
    public void testDoParseInvalidResponse() throws MalformedFrameException, IllegalRawDataException {
        Dot11AuthenticationFrame frame = new Dot11AuthenticationFrameParser(new MetricRegistry())
                .doParse(Frames.AUTH_INVALID_RESPONSE_STAGE_1_PAYLOAD, Frames.AUTH_INVALID_RESPONSE_STAGE_1_HEADER, META_NO_WEP);

        assertEquals(frame.algorithm(), Dot11AuthenticationFrameParser.ALGORITHM_TYPE.OPEN_SYSTEM);
        assertEquals(frame.transmitter(), "ac:5f:3e:b9:5d:be");
        assertEquals(frame.destination(), "e0:22:03:f8:a3:39");
        assertEquals(frame.statusString(), "Invalid/Unknown (5)");
        assertEquals((short) frame.statusCode(), (short) 5);
        assertEquals((short) frame.transactionSequence(), (short) 1);
    }

    @Test
    public void testDoParseSuccessfulAuthWEPStage1() throws MalformedFrameException, IllegalRawDataException {
        Dot11AuthenticationFrame frame = new Dot11AuthenticationFrameParser(new MetricRegistry())
                .doParse(Frames.AUTH_SUCCESS_WEP_STAGE_1_PAYLOAD, Frames.AUTH_SUCCESS_WEP_STAGE_1_HEADER, META_NO_WEP);

        assertEquals(frame.algorithm(), Dot11AuthenticationFrameParser.ALGORITHM_TYPE.SHARED_KEY);
        assertEquals(frame.transmitter(), "e0:33:8e:34:9e:73");
        assertEquals(frame.destination(), "f2:e5:6f:7c:84:6d");
        assertEquals(frame.statusString(), "success");
        assertEquals((short) frame.statusCode(), (short) 0);
        assertEquals((short) frame.transactionSequence(), (short) 1);
    }

    @Test
    public void testDoParseSuccessfulAuthWEPStage2() throws MalformedFrameException, IllegalRawDataException {
        Dot11AuthenticationFrame frame = new Dot11AuthenticationFrameParser(new MetricRegistry())
                .doParse(Frames.AUTH_SUCCESS_WEP_STAGE_2_PAYLOAD, Frames.AUTH_SUCCESS_WEP_STAGE_2_HEADER, META_NO_WEP);

        assertEquals(frame.algorithm(), Dot11AuthenticationFrameParser.ALGORITHM_TYPE.SHARED_KEY);
        assertEquals(frame.transmitter(), "f2:e5:6f:7c:84:6d");
        assertEquals(frame.destination(), "e0:33:8e:34:9e:73");
        assertEquals(frame.statusString(), "success");
        assertEquals((short) frame.statusCode(), (short) 0);
        assertEquals((short) frame.transactionSequence(), (short) 2);
    }

    @Test
    public void testDoParseSuccessfulAuthWEPStage4() throws MalformedFrameException, IllegalRawDataException {
        Dot11AuthenticationFrame frame = new Dot11AuthenticationFrameParser(new MetricRegistry())
                .doParse(Frames.AUTH_SUCCESS_WEP_STAGE_4_PAYLOAD, Frames.AUTH_SUCCESS_WEP_STAGE_4_HEADER, META_NO_WEP);

        assertEquals(frame.algorithm(), Dot11AuthenticationFrameParser.ALGORITHM_TYPE.SHARED_KEY);
        assertEquals(frame.transmitter(), "f2:e5:6f:7c:84:6d");
        assertEquals(frame.destination(), "e0:33:8e:34:9e:73");
        assertEquals(frame.statusString(), "success");
        assertEquals((short) frame.statusCode(), (short) 0);
        assertEquals((short) frame.transactionSequence(), (short) 4);
    }

    @Test
    public void testDoParseFailedAuthWEPStage4() throws MalformedFrameException, IllegalRawDataException {
        Dot11AuthenticationFrame frame = new Dot11AuthenticationFrameParser(new MetricRegistry())
                .doParse(Frames.AUTH_FAILED_WEP_STAGE_4_PAYLOAD, Frames.AUTH_FAILED_WEP_STAGE_4_HEADER, META_NO_WEP);
        
        assertEquals(frame.algorithm(), Dot11AuthenticationFrameParser.ALGORITHM_TYPE.SHARED_KEY);
        assertEquals(frame.transmitter(), "f2:e5:6f:7c:84:6d");
        assertEquals(frame.destination(), "e0:33:8e:34:9e:73");
        assertEquals(frame.statusString(), "failure");
        assertEquals((short) frame.statusCode(), (short) 1);
        assertEquals((short) frame.transactionSequence(), (short) 4);
    }

}