package horse.wtf.nzyme.dot11.handlers;

import com.codahale.metrics.MetricRegistry;
import horse.wtf.nzyme.MockNzyme;
import horse.wtf.nzyme.Nzyme;
import horse.wtf.nzyme.dot11.MalformedFrameException;
import horse.wtf.nzyme.dot11.frames.Dot11ProbeResponseFrame;
import horse.wtf.nzyme.dot11.parsers.Dot11ProbeResponseFrameParser;
import horse.wtf.nzyme.dot11.parsers.Frames;
import horse.wtf.nzyme.dot11.probes.Dot11MockProbe;
import horse.wtf.nzyme.dot11.probes.Dot11Probe;
import horse.wtf.nzyme.notifications.Notification;
import horse.wtf.nzyme.notifications.uplinks.misc.LoopbackUplink;
import horse.wtf.nzyme.statistics.Statistics;
import org.pcap4j.packet.IllegalRawDataException;
import org.testng.annotations.Test;

import static org.testng.Assert.*;

public class Dot11ProbeResponseFrameHandlerTest extends FrameHandlerTest {

    @Test
    public void testDoHandle() throws MalformedFrameException, IllegalRawDataException {
        Nzyme nzyme = new MockNzyme();
        Dot11Probe probe = new Dot11MockProbe(nzyme, CONFIG_STANDARD, new Statistics(nzyme));
        LoopbackUplink loopback = new LoopbackUplink();
        nzyme.registerUplink(loopback);

        Dot11ProbeResponseFrame frame = new Dot11ProbeResponseFrameParser(new MetricRegistry())
                .parse(Frames.PROBE_RESP_1_PAYLOAD, Frames.PROBE_RESP_1_HEADER, META_NO_WEP);

        new Dot11ProbeResponseFrameHandler(probe).handle(frame);

        Notification n = loopback.getLastNotification();

        assertEquals(n.getAdditionalFields().size(), 9);
        assertEquals(n.getMessage(), "b0:93:5b:1d:c8:f1 responded to probe request from 3c:8d:20:52:e4:87 for Home 5F48");
        assertEquals(n.getAdditionalFields().get("channel"), 1);
        assertEquals(n.getAdditionalFields().get("destination"), "3c:8d:20:52:e4:87");
        assertEquals(n.getAdditionalFields().get("transmitter"), "b0:93:5b:1d:c8:f1");
        assertEquals(n.getAdditionalFields().get("ssid"), "Home 5F48");
        assertEquals(n.getAdditionalFields().get("security_full"), "WPA2-PSK-CCMP");
        assertEquals(n.getAdditionalFields().get("is_wpa1"), false);
        assertEquals(n.getAdditionalFields().get("is_wpa2"), true);
        assertEquals(n.getAdditionalFields().get("is_wps"), true);
        assertEquals(n.getAdditionalFields().get("subtype"), "probe-resp");
    }

    @Test
    public void testGetName() {
        assertEquals(new Dot11ProbeResponseFrameHandler(null).getName(), "probe-resp");
    }

}