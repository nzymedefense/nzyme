package horse.wtf.nzyme.dot11.handlers;

import com.codahale.metrics.MetricRegistry;
import horse.wtf.nzyme.MockNzyme;
import horse.wtf.nzyme.Nzyme;
import horse.wtf.nzyme.dot11.MalformedFrameException;
import horse.wtf.nzyme.dot11.frames.Dot11BeaconFrame;
import horse.wtf.nzyme.dot11.parsers.Dot11BeaconFrameParser;
import horse.wtf.nzyme.dot11.parsers.Frames;
import horse.wtf.nzyme.dot11.probes.Dot11MockProbe;
import horse.wtf.nzyme.dot11.probes.Dot11Probe;
import horse.wtf.nzyme.notifications.Notification;
import horse.wtf.nzyme.notifications.uplinks.misc.LoopbackUplink;
import horse.wtf.nzyme.statistics.Statistics;
import org.pcap4j.packet.IllegalRawDataException;
import org.testng.annotations.Test;

import static org.testng.Assert.*;

public class Dot11BeaconFrameHandlerTest extends FrameHandlerTest {

    @Test
    public void testDoHandle() throws MalformedFrameException, IllegalRawDataException {
        Nzyme nzyme = new MockNzyme();
        Dot11Probe probe = new Dot11MockProbe(nzyme, CONFIG_STANDARD, new Statistics(nzyme));
        LoopbackUplink loopback = new LoopbackUplink();
        nzyme.registerUplink(loopback);

        Dot11BeaconFrame frame = new Dot11BeaconFrameParser(new MetricRegistry())
                .parse(Frames.BEACON_1_PAYLOAD, Frames.BEACON_1_HEADER, META_NO_WEP);

        new Dot11BeaconFrameHandler(probe).handle(frame);

        Notification n = loopback.getLastNotification();

        assertEquals(n.getAdditionalFields().size(), 9);
        assertEquals(n.getMessage(), "Received beacon from 00:c0:ca:95:68:3b for SSID WTF");
        assertEquals(n.getAdditionalFields().get("channel"), 1);
        assertEquals(n.getAdditionalFields().get("transmitter"), "00:c0:ca:95:68:3b");
        assertEquals(n.getAdditionalFields().get("transmitter_fingerprint"), "dfac3abce0c722f9609343f7dfa208afa51a1c7decbd2eb6f96c78051f0a594b");
        assertEquals(n.getAdditionalFields().get("ssid"), "WTF");
        assertEquals(n.getAdditionalFields().get("security_full"), "WPA1-EAM-PSK-CCMP, WPA2-EAM-PSK-CCMP");
        assertEquals(n.getAdditionalFields().get("is_wpa1"), true);
        assertEquals(n.getAdditionalFields().get("is_wpa2"), true);
        assertEquals(n.getAdditionalFields().get("is_wps"), false);
        assertEquals(n.getAdditionalFields().get("subtype"), "beacon");
    }

    @Test
    public void testDoHandleBroadcast() throws MalformedFrameException, IllegalRawDataException {
        Nzyme nzyme = new MockNzyme();
        Dot11Probe probe = new Dot11MockProbe(nzyme, CONFIG_STANDARD, new Statistics(nzyme));
        LoopbackUplink loopback = new LoopbackUplink();
        nzyme.registerUplink(loopback);

        Dot11BeaconFrame frame = new Dot11BeaconFrameParser(new MetricRegistry())
                .parse(Frames.BEACON_4_PAYLOAD, Frames.BEACON_4_HEADER, META_NO_WEP);

        new Dot11BeaconFrameHandler(probe).handle(frame);

        Notification n = loopback.getLastNotification();

        assertEquals(n.getAdditionalFields().size(), 9);
        assertEquals(n.getMessage(), "Received broadcast beacon from 24:a4:3c:7d:01:cc");
        assertEquals(n.getAdditionalFields().get("channel"), 1);
        assertEquals(n.getAdditionalFields().get("transmitter"), "24:a4:3c:7d:01:cc");
        assertEquals(n.getAdditionalFields().get("transmitter_fingerprint"), "52f519b9e8b1a4901a3db02407ff62246f5cfc2d5ddadd5a10e5230524ef04a9");
        assertEquals(n.getAdditionalFields().get("ssid"), "[no SSID]");
        assertEquals(n.getAdditionalFields().get("security_full"), "NONE");
        assertEquals(n.getAdditionalFields().get("is_wpa1"), false);
        assertEquals(n.getAdditionalFields().get("is_wpa2"), false);
        assertEquals(n.getAdditionalFields().get("is_wps"), false);
        assertEquals(n.getAdditionalFields().get("subtype"), "beacon");
    }

    @Test
    public void testGetName() {
        assertEquals(new Dot11BeaconFrameHandler(null).getName(), "beacon");
    }

}