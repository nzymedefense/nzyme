package horse.wtf.nzyme.dot11.handlers;

import com.codahale.metrics.MetricRegistry;
import horse.wtf.nzyme.MockNzyme;
import horse.wtf.nzyme.NzymeLeader;
import horse.wtf.nzyme.dot11.MalformedFrameException;
import horse.wtf.nzyme.dot11.frames.Dot11AssociationResponseFrame;
import horse.wtf.nzyme.dot11.parsers.Dot11AssociationResponseFrameParser;
import horse.wtf.nzyme.dot11.parsers.Frames;
import horse.wtf.nzyme.dot11.probes.Dot11MockProbe;
import horse.wtf.nzyme.dot11.probes.Dot11Probe;
import horse.wtf.nzyme.notifications.Notification;
import horse.wtf.nzyme.notifications.uplinks.misc.LoopbackUplink;
import horse.wtf.nzyme.statistics.Statistics;
import org.pcap4j.packet.IllegalRawDataException;
import org.testng.annotations.Test;

import static org.testng.Assert.*;

public class Dot11AssociationResponseFrameHandlerTest extends FrameHandlerTest {

    @Test
    public void testDoHandleSuccessResponse() throws MalformedFrameException, IllegalRawDataException {
        NzymeLeader nzyme = new MockNzyme();
        Dot11Probe probe = new Dot11MockProbe(CONFIG_STANDARD, new Statistics(nzyme), nzyme.getMetrics());
        LoopbackUplink loopback = new LoopbackUplink();
        nzyme.registerUplink(loopback);

        Dot11AssociationResponseFrame frame = new Dot11AssociationResponseFrameParser(new MetricRegistry())
                .parse(Frames.ASSOC_RESP_SUCCESS_1_PAYLOAD, Frames.ASSOC_RESP_SUCCESS_1_HEADER, META_NO_WEP);

        new Dot11AssociationResponseFrameHandler(probe, nzyme).handle(frame);

        Notification n = loopback.getLastNotification();

        assertEquals(n.getAdditionalFields().size(), 6);
        assertEquals(n.getMessage(),"88:96:4e:4d:77:80 answered association request from 5c:77:76:d3:26:45. Response: SUCCESS (0)");
        assertEquals(n.getAdditionalFields().get("channel"), 1);
        assertEquals(n.getAdditionalFields().get("transmitter"), "88:96:4e:4d:77:80");
        assertEquals(n.getAdditionalFields().get("destination"), "5c:77:76:d3:26:45");
        assertEquals(n.getAdditionalFields().get("response_code"), (short)0);
        assertEquals(n.getAdditionalFields().get("response_string"), "success");
        assertEquals(n.getAdditionalFields().get("subtype"), "assoc-resp");
    }

    @Test
    public void testDoHandleFailResponse() throws MalformedFrameException, IllegalRawDataException {
        NzymeLeader nzyme = new MockNzyme();
        Dot11Probe probe = new Dot11MockProbe(CONFIG_STANDARD, new Statistics(nzyme), nzyme.getMetrics());
        LoopbackUplink loopback = new LoopbackUplink();
        nzyme.registerUplink(loopback);

        Dot11AssociationResponseFrame frame = new Dot11AssociationResponseFrameParser(new MetricRegistry())
                .parse(Frames.ASSOC_RESP_FAILED_1_PAYLOAD, Frames.ASSOC_RESP_FAILED_1_HEADER, META_NO_WEP);

        new Dot11AssociationResponseFrameHandler(probe, nzyme).handle(frame);

        Notification n = loopback.getLastNotification();

        assertEquals(n.getAdditionalFields().size(), 6);
        assertEquals(n.getMessage(),"88:96:4e:4d:77:80 answered association request from 5c:77:76:d3:26:45. Response: REFUSED (1)");
        assertEquals(n.getAdditionalFields().get("channel"), 1);
        assertEquals(n.getAdditionalFields().get("transmitter"), "88:96:4e:4d:77:80");
        assertEquals(n.getAdditionalFields().get("destination"), "5c:77:76:d3:26:45");
        assertEquals(n.getAdditionalFields().get("response_code"), (short)1);
        assertEquals(n.getAdditionalFields().get("response_string"), "refused");
        assertEquals(n.getAdditionalFields().get("subtype"), "assoc-resp");
    }

    @Test
    public void testGetName() {
        assertEquals(new Dot11AssociationResponseFrameHandler(null, new MockNzyme()).getName(), "assoc-resp");
    }

}