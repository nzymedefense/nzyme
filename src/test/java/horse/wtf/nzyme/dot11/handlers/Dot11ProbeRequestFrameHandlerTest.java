package horse.wtf.nzyme.dot11.handlers;

import com.codahale.metrics.MetricRegistry;
import horse.wtf.nzyme.MockNzyme;
import horse.wtf.nzyme.NzymeLeader;
import horse.wtf.nzyme.dot11.MalformedFrameException;
import horse.wtf.nzyme.dot11.frames.Dot11ProbeRequestFrame;
import horse.wtf.nzyme.dot11.parsers.Dot11ProbeRequestFrameParser;
import horse.wtf.nzyme.dot11.parsers.Frames;
import horse.wtf.nzyme.dot11.probes.Dot11MockProbe;
import horse.wtf.nzyme.dot11.probes.Dot11Probe;
import horse.wtf.nzyme.notifications.Notification;
import horse.wtf.nzyme.notifications.uplinks.misc.LoopbackUplink;
import horse.wtf.nzyme.statistics.Statistics;
import org.pcap4j.packet.IllegalRawDataException;
import org.testng.annotations.Test;

import static org.testng.Assert.*;

public class Dot11ProbeRequestFrameHandlerTest extends FrameHandlerTest {

    @Test
    public void testDoHandle() throws MalformedFrameException, IllegalRawDataException {
        NzymeLeader nzyme = new MockNzyme();
        Dot11Probe probe = new Dot11MockProbe(nzyme, CONFIG_STANDARD, new Statistics(nzyme));
        LoopbackUplink loopback = new LoopbackUplink();
        nzyme.registerUplink(loopback);

        Dot11ProbeRequestFrame frame = new Dot11ProbeRequestFrameParser(new MetricRegistry())
                .parse(Frames.PROBE_REQ_1_PAYLOAD, Frames.PROBE_REQ_1_HEADER, META_NO_WEP);

        new Dot11ProbeRequestFrameHandler(probe).handle(frame);

        Notification n = loopback.getLastNotification();

        assertEquals(n.getAdditionalFields().size(), 4);
        assertEquals(n.getMessage(), "Probe request: 3c:8d:20:25:20:e9 is looking for ATT6r8YXW9");
        assertEquals(n.getAdditionalFields().get("channel"), 1);
        assertEquals(n.getAdditionalFields().get("ssid"), "ATT6r8YXW9");
        assertEquals(n.getAdditionalFields().get("transmitter"), "3c:8d:20:25:20:e9");
        assertEquals(n.getAdditionalFields().get("subtype"), "probe-req");
    }

    @Test
    public void testDoHandleBroadcastFrame() throws MalformedFrameException, IllegalRawDataException {
        NzymeLeader nzyme = new MockNzyme();
        Dot11Probe probe = new Dot11MockProbe(nzyme, CONFIG_STANDARD, new Statistics(nzyme));
        LoopbackUplink loopback = new LoopbackUplink();
        nzyme.registerUplink(loopback);

        Dot11ProbeRequestFrame frame = new Dot11ProbeRequestFrameParser(new MetricRegistry())
                .parse(Frames.PROBE_REQ_BROADCAST_1_PAYLOAD, Frames.PROBE_REQ_BROADCAST_1_HEADER, META_NO_WEP);

        new Dot11ProbeRequestFrameHandler(probe).doHandle(frame);

        Notification n = loopback.getLastNotification();

        assertEquals(n.getAdditionalFields().size(), 4);
        assertEquals(n.getMessage(), "Probe request: f8:da:0c:2e:af:1c is looking for any network. (null probe request)");
        assertEquals(n.getAdditionalFields().get("channel"), 1);
        assertEquals(n.getAdditionalFields().get("ssid"), "[no SSID]");
        assertEquals(n.getAdditionalFields().get("transmitter"), "f8:da:0c:2e:af:1c");
        assertEquals(n.getAdditionalFields().get("subtype"), "probe-req");
    }

    @Test
    public void testGetName() {
        assertEquals(new Dot11ProbeRequestFrameHandler(null).getName(), "probe-req");
    }

}