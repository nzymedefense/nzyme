package horse.wtf.nzyme.dot11.handlers;

import com.codahale.metrics.MetricRegistry;
import horse.wtf.nzyme.MockNzyme;
import horse.wtf.nzyme.Nzyme;
import horse.wtf.nzyme.dot11.MalformedFrameException;
import horse.wtf.nzyme.dot11.frames.Dot11DeauthenticationFrame;
import horse.wtf.nzyme.dot11.parsers.Dot11DeauthenticationFrameParser;
import horse.wtf.nzyme.dot11.parsers.Frames;
import horse.wtf.nzyme.dot11.probes.Dot11MockProbe;
import horse.wtf.nzyme.dot11.probes.Dot11Probe;
import horse.wtf.nzyme.notifications.Notification;
import horse.wtf.nzyme.notifications.uplinks.misc.LoopbackUplink;
import horse.wtf.nzyme.statistics.Statistics;
import org.pcap4j.packet.IllegalRawDataException;
import org.testng.annotations.Test;

import static org.testng.Assert.*;

public class Dot11DeauthenticationFrameHandlerTest extends FrameHandlerTest {

    @Test
    public void testDoHandle() throws MalformedFrameException, IllegalRawDataException {
        Nzyme nzyme = new MockNzyme();
        Dot11Probe probe = new Dot11MockProbe(nzyme, CONFIG_STANDARD, new Statistics());
        LoopbackUplink loopback = new LoopbackUplink();
        probe.registerUplink(loopback);

        Dot11DeauthenticationFrame frame = new Dot11DeauthenticationFrameParser(new MetricRegistry())
                .parse(Frames.DEAUTH_1_PAYLOAD, Frames.DEAUTH_1_HEADER, META_NO_WEP);

        new Dot11DeauthenticationFrameHandler(probe).handle(frame);

        Notification n = loopback.getLastNotification();

        assertEquals(n.getAdditionalFields().size(), 7);
        assertEquals(n.getMessage(), "Deauth: Transmitter b0:93:5b:1d:c8:f1 is deauthenticating e4:b2:fb:27:50:15 from BSSID b0:93:5b:1d:c8:f1 (Class 2 frame received from nonauthenticated STA)");
        assertEquals(n.getAdditionalFields().get("channel"), 1);
        assertEquals(n.getAdditionalFields().get("transmitter"), "b0:93:5b:1d:c8:f1");
        assertEquals(n.getAdditionalFields().get("destination"), "e4:b2:fb:27:50:15");
        assertEquals(n.getAdditionalFields().get("bssid"), "b0:93:5b:1d:c8:f1");
        assertEquals(n.getAdditionalFields().get("reason_code"), (short)6);
        assertEquals(n.getAdditionalFields().get("reason_string"), "Class 2 frame received from nonauthenticated STA");
        assertEquals(n.getAdditionalFields().get("subtype"), "deauth");
    }

    @Test
    public void testGetName() {
        assertEquals(new Dot11DeauthenticationFrameHandler(null).getName(), "deauth");
    }

}