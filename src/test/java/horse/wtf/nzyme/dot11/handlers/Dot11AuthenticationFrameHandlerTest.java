package horse.wtf.nzyme.dot11.handlers;

import com.codahale.metrics.MetricRegistry;
import horse.wtf.nzyme.MockNzyme;
import horse.wtf.nzyme.Nzyme;
import horse.wtf.nzyme.dot11.MalformedFrameException;
import horse.wtf.nzyme.dot11.frames.Dot11AuthenticationFrame;
import horse.wtf.nzyme.dot11.parsers.Dot11AuthenticationFrameParser;
import horse.wtf.nzyme.dot11.parsers.Frames;
import horse.wtf.nzyme.dot11.probes.Dot11MockProbe;
import horse.wtf.nzyme.dot11.probes.Dot11Probe;
import horse.wtf.nzyme.notifications.Notification;
import horse.wtf.nzyme.notifications.uplinks.misc.LoopbackUplink;
import horse.wtf.nzyme.statistics.Statistics;
import org.pcap4j.packet.IllegalRawDataException;
import org.testng.annotations.Test;

import static org.testng.Assert.*;

public class Dot11AuthenticationFrameHandlerTest extends FrameHandlerTest {

    @Test
    public void testDoHandleOpenSystemSeq1() throws MalformedFrameException, IllegalRawDataException {
        Nzyme nzyme = new MockNzyme();
        Dot11Probe probe = new Dot11MockProbe(nzyme, CONFIG_STANDARD, new Statistics());
        LoopbackUplink loopback = new LoopbackUplink();
        probe.registerUplink(loopback);

        Dot11AuthenticationFrame frame = new Dot11AuthenticationFrameParser(new MetricRegistry())
                .parse(Frames.AUTH_SUCCESS_STAGE_1_PAYLOAD, Frames.AUTH_SUCCESS_STAGE_1_HEADER, META_NO_WEP);

        new Dot11AuthenticationFrameHandler(probe).handle(frame);

        Notification n = loopback.getLastNotification();

        assertEquals(n.getAdditionalFields().size(), 7);
        assertEquals(n.getMessage(), "ac:5f:3e:b9:5d:be is requesting to authenticate with Open System (Open, WPA, WPA2, ...) at e0:22:03:f8:a3:39");
        assertEquals(n.getAdditionalFields().get("channel"), 1);
        assertEquals(n.getAdditionalFields().get("transmitter"), "ac:5f:3e:b9:5d:be");
        assertEquals(n.getAdditionalFields().get("destination"), "e0:22:03:f8:a3:39");
        assertEquals(n.getAdditionalFields().get("authentication_algorithm"), "open_system");
        assertEquals(n.getAdditionalFields().get("transaction_sequence_number"), (short) 1);
        assertEquals(n.getAdditionalFields().get("is_wep"), false);
        assertEquals(n.getAdditionalFields().get("subtype"), "auth");
    }

    @Test
    public void testDoHandleOpenSystemSeq2() throws MalformedFrameException, IllegalRawDataException {
        Nzyme nzyme = new MockNzyme();
        Dot11Probe probe = new Dot11MockProbe(nzyme, CONFIG_STANDARD, new Statistics());
        LoopbackUplink loopback = new LoopbackUplink();
        probe.registerUplink(loopback);

        Dot11AuthenticationFrame frame = new Dot11AuthenticationFrameParser(new MetricRegistry())
                .parse(Frames.AUTH_SUCCESS_STAGE_2_PAYLOAD, Frames.AUTH_SUCCESS_STAGE_2_HEADER, META_NO_WEP);

        new Dot11AuthenticationFrameHandler(probe).handle(frame);

        Notification n = loopback.getLastNotification();

        assertEquals(n.getAdditionalFields().size(), 9);
        assertEquals(n.getMessage(), "2c:5d:93:04:5c:09 is responding to Open System (Open, WPA, WPA2, ...) authentication request from 64:76:ba:d8:5d:ab. (success)");
        assertEquals(n.getAdditionalFields().get("channel"), 1);
        assertEquals(n.getAdditionalFields().get("transmitter"), "2c:5d:93:04:5c:09");
        assertEquals(n.getAdditionalFields().get("destination"), "64:76:ba:d8:5d:ab");
        assertEquals(n.getAdditionalFields().get("response_code"), (short) 0);
        assertEquals(n.getAdditionalFields().get("response_string"), "success");
        assertEquals(n.getAdditionalFields().get("authentication_algorithm"), "open_system");
        assertEquals(n.getAdditionalFields().get("transaction_sequence_number"), (short) 2);
        assertEquals(n.getAdditionalFields().get("is_wep"), false);
        assertEquals(n.getAdditionalFields().get("subtype"), "auth");
    }

    @Test
    public void testGetName() {
        assertEquals(new Dot11AuthenticationFrameHandler(null).getName(), "auth");
    }

}