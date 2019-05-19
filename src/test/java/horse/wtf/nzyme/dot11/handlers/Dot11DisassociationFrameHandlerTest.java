package horse.wtf.nzyme.dot11.handlers;

import com.codahale.metrics.MetricRegistry;
import horse.wtf.nzyme.MockNzyme;
import horse.wtf.nzyme.Nzyme;
import horse.wtf.nzyme.dot11.MalformedFrameException;
import horse.wtf.nzyme.dot11.frames.Dot11DisassociationFrame;
import horse.wtf.nzyme.dot11.parsers.Dot11DisassociationFrameParser;
import horse.wtf.nzyme.dot11.parsers.Frames;
import horse.wtf.nzyme.dot11.probes.Dot11MockProbe;
import horse.wtf.nzyme.dot11.probes.Dot11Probe;
import horse.wtf.nzyme.notifications.Notification;
import horse.wtf.nzyme.notifications.uplinks.misc.LoopbackUplink;
import horse.wtf.nzyme.statistics.Statistics;
import org.pcap4j.packet.IllegalRawDataException;
import org.testng.annotations.Test;

import static org.testng.Assert.*;

public class Dot11DisassociationFrameHandlerTest extends FrameHandlerTest {

    @Test
    public void testDoHandle() throws MalformedFrameException, IllegalRawDataException {
        Nzyme nzyme = new MockNzyme();
        Dot11Probe probe = new Dot11MockProbe(nzyme, CONFIG_STANDARD, new Statistics());
        LoopbackUplink loopback = new LoopbackUplink();
        probe.registerUplink(loopback);

        Dot11DisassociationFrame frame = new Dot11DisassociationFrameParser(new MetricRegistry())
                .parse(Frames.DISASSOC_1_PAYLOAD, Frames.DISASSOC_1_HEADER, META_NO_WEP);

        new Dot11DisassociationFrameHandler(probe).handle(frame);

        Notification n = loopback.getLastNotification();
        
        assertEquals(n.getAdditionalFields().size(), 6);
        assertEquals(n.getMessage(), "b4:fb:e4:41:f6:45 is disassociating from b0:70:2d:56:1c:f7 (Disassociated because sending STA is leaving (or has left) BSS)");
        assertEquals(n.getAdditionalFields().get("_channel"), 1);
        assertEquals(n.getAdditionalFields().get("_transmitter"), "b4:fb:e4:41:f6:45");
        assertEquals(n.getAdditionalFields().get("_destination"), "b0:70:2d:56:1c:f7");
        assertEquals(n.getAdditionalFields().get("_reason_code"), (short)8);
        assertEquals(n.getAdditionalFields().get("_reason_string"), "Disassociated because sending STA is leaving (or has left) BSS");
        assertEquals(n.getAdditionalFields().get("_subtype"), "disassoc");
    }

    @Test
    public void testGetName() {
        assertEquals(new Dot11DisassociationFrameHandler(null).getName(), "disassoc");
    }

}