package horse.wtf.nzyme.dot11.parsers;

import com.codahale.metrics.MetricRegistry;
import horse.wtf.nzyme.dot11.Dot11MetaInformation;
import horse.wtf.nzyme.dot11.MalformedFrameException;
import horse.wtf.nzyme.dot11.anonymization.Anonymizer;
import horse.wtf.nzyme.dot11.frames.Dot11BeaconFrame;
import org.pcap4j.packet.IllegalRawDataException;
import org.testng.annotations.Test;

import static org.testng.Assert.*;

public class Dot11BeaconFrameParserTest extends FrameParserTest {

    @Test
    public void testDoParseWPA1WPA2EAMPSKCCMP() throws MalformedFrameException, IllegalRawDataException {
        Dot11BeaconFrame frame = new Dot11BeaconFrameParser(new MetricRegistry(), new Anonymizer(false, ""))
                .doParse(Frames.BEACON_1_PAYLOAD, Frames.BEACON_1_HEADER, META_NO_WEP);

        assertEquals(frame.ssid(), "WTF");
        assertEquals(frame.transmitter(), "00:c0:ca:95:68:3b");
        assertEquals(frame.transmitterFingerprint(), "dfac3abce0c722f9609343f7dfa208afa51a1c7decbd2eb6f96c78051f0a594b");
        assertEquals(frame.taggedParameters().isWPS(), false);
        assertEquals(frame.taggedParameters().getFullSecurityString(), "WPA1-EAM-PSK-CCMP, WPA2-EAM-PSK-CCMP");
    }

    @Test
    public void testDoParseWPA1WPA2EAMPSKCCMPTKIP() throws MalformedFrameException, IllegalRawDataException {
        Dot11BeaconFrame frame = new Dot11BeaconFrameParser(new MetricRegistry(), new Anonymizer(false, ""))
                .doParse(Frames.BEACON_2_PAYLOAD, Frames.BEACON_2_HEADER, META_NO_WEP);

        assertEquals(frame.ssid(), "WTF");
        assertEquals(frame.transmitter(), "00:c0:ca:95:68:3b");
        assertEquals(frame.transmitterFingerprint(), "6586b438a7ef3c680c39983b8f2a079e53962f12b302f5ffeeaf4daad2e8ca33");
        assertEquals(frame.taggedParameters().isWPS(), false);
        assertEquals(frame.taggedParameters().getFullSecurityString(), "WPA1-EAM-PSK-CCMP-TKIP, WPA2-EAM-PSK-CCMP-TKIP");
    }

    @Test
    public void testDoParseNoSecurity() throws MalformedFrameException, IllegalRawDataException {
        Dot11BeaconFrame frame = new Dot11BeaconFrameParser(new MetricRegistry(), new Anonymizer(false, ""))
                .doParse(Frames.BEACON_3_PAYLOAD, Frames.BEACON_3_HEADER, META_NO_WEP);

        assertEquals(frame.ssid(), "United_Wi-Fi");
        assertEquals(frame.transmitter(), "06:0d:2d:c9:36:23");
        assertEquals(frame.transmitterFingerprint(), "c9ed4adc12dc3e17208446b6a10070b70a73b9ce3a99215e05426faea6de91c7");
        assertEquals(frame.taggedParameters().isWPS(), false);
        assertEquals(frame.taggedParameters().getFullSecurityString(), "NONE");
    }

    @Test
    public void testdoParseBroadcast() throws MalformedFrameException, IllegalRawDataException {
        Dot11BeaconFrame frame = new Dot11BeaconFrameParser(new MetricRegistry(), new Anonymizer(false, ""))
                .doParse(Frames.BEACON_4_PAYLOAD, Frames.BEACON_4_HEADER, META_NO_WEP);

        assertNull(frame.ssid());
        assertEquals(frame.transmitter(), "24:a4:3c:7d:01:cc");
        assertEquals(frame.transmitterFingerprint(), "52f519b9e8b1a4901a3db02407ff62246f5cfc2d5ddadd5a10e5230524ef04a9");
        assertEquals(frame.taggedParameters().isWPS(), false);
        assertEquals(frame.taggedParameters().getFullSecurityString(), "NONE");
    }

}