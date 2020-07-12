package horse.wtf.nzyme.dot11.handlers;

import com.codahale.metrics.MetricRegistry;
import horse.wtf.nzyme.MockNzyme;
import horse.wtf.nzyme.NzymeLeader;
import horse.wtf.nzyme.dot11.MalformedFrameException;
import horse.wtf.nzyme.dot11.anonymization.Anonymizer;
import horse.wtf.nzyme.dot11.frames.Dot11AssociationRequestFrame;
import horse.wtf.nzyme.dot11.parsers.Dot11AssociationRequestFrameParser;
import horse.wtf.nzyme.dot11.parsers.Frames;
import horse.wtf.nzyme.dot11.probes.Dot11MockProbe;
import horse.wtf.nzyme.dot11.probes.Dot11Probe;
import horse.wtf.nzyme.notifications.Notification;
import horse.wtf.nzyme.notifications.uplinks.misc.LoopbackUplink;
import horse.wtf.nzyme.statistics.Statistics;
import org.pcap4j.packet.IllegalRawDataException;
import org.testng.annotations.Test;

import static org.testng.Assert.*;

public class Dot11AssociationRequestFrameHandlerTest extends FrameHandlerTest {

    @Test
    public void testDoHandle() throws MalformedFrameException, IllegalRawDataException {
        NzymeLeader nzyme = new MockNzyme();
        Dot11Probe probe = new Dot11MockProbe(CONFIG_STANDARD, new Statistics(nzyme), nzyme.getMetrics());
        LoopbackUplink loopback = new LoopbackUplink();
        nzyme.registerUplink(loopback);

        Dot11AssociationRequestFrame frame = new Dot11AssociationRequestFrameParser(new MetricRegistry(), new Anonymizer(false, ""))
                .parse(Frames.ASSOC_REQ_1_PAYLOAD, Frames.ASSOC_REQ_1_HEADER, META_NO_WEP);

        new Dot11AssociationRequestFrameHandler(probe, nzyme).handle(frame);

        Notification n = loopback.getLastNotification();

        assertEquals(n.getAdditionalFields().size(), 5);
        assertEquals(n.getMessage(), "ac:81:12:d2:26:7e is requesting to associate with ATT4Q5FBC3 at 14:ed:bb:79:97:4d");
        assertEquals(n.getAdditionalFields().get("channel"), 1);
        assertEquals(n.getAdditionalFields().get("transmitter"), "ac:81:12:d2:26:7e");
        assertEquals(n.getAdditionalFields().get("destination"), "14:ed:bb:79:97:4d");
        assertEquals(n.getAdditionalFields().get("ssid"), "ATT4Q5FBC3");
        assertEquals(n.getAdditionalFields().get("subtype"), "assoc-req");
    }

    @Test
    public void testGetName() {
        assertEquals(new Dot11AssociationRequestFrameHandler(null, new MockNzyme()).getName(), "assoc-req");
    }

}