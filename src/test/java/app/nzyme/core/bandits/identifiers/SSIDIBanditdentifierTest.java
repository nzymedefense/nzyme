package app.nzyme.core.bandits.identifiers;

import com.codahale.metrics.MetricRegistry;
import app.nzyme.core.dot11.MalformedFrameException;
import app.nzyme.core.dot11.anonymization.Anonymizer;
import app.nzyme.core.dot11.parsers.Dot11BeaconFrameParser;
import app.nzyme.core.dot11.parsers.Dot11DeauthenticationFrameParser;
import app.nzyme.core.dot11.parsers.Dot11ProbeResponseFrameParser;
import app.nzyme.core.dot11.parsers.Frames;
import org.pcap4j.packet.IllegalRawDataException;
import org.testng.annotations.Test;

import java.util.ArrayList;
import java.util.Optional;

import static org.testng.Assert.*;

public class SSIDIBanditdentifierTest extends BanditIdentifierTest {

    @Test
    public void testDescriptorSingleSSIDs() {
        BanditIdentifier id = new SSIDIBanditdentifier(new ArrayList<String>() {{
            add("WTF");
        }}, null, null);

        assertEquals(id.descriptor(), BanditIdentifierDescriptor.create(
                BanditIdentifier.TYPE.SSID,
                "Matches if the SSID advertised by frame is one of the configured SSIDs. (multiple SSIDs can be entered, separated by comma)",
                "frame.ssid IN [\"WTF\"]"
        ));
    }

    @Test
    public void testDescriptorMultipleSSIDs() {
        BanditIdentifier id = new SSIDIBanditdentifier(new ArrayList<String>() {{
            add("WTF");
            add("foo");
        }}, null, null);

        assertEquals(id.descriptor(), BanditIdentifierDescriptor.create(
                BanditIdentifier.TYPE.SSID,
                "Matches if the SSID advertised by frame is one of the configured SSIDs. (multiple SSIDs can be entered, separated by comma)",
                "frame.ssid IN [\"WTF\",\"foo\"]"
        ));
    }

    @Test
    public void testMatchesBeacon() throws MalformedFrameException, IllegalRawDataException {
        BanditIdentifier id = new SSIDIBanditdentifier(new ArrayList<String>() {{
            add("WTF");
            add("foo");
        }}, null, null);

        Optional<Boolean> result = id.matches(new Dot11BeaconFrameParser(new MetricRegistry(), new Anonymizer(false, "")).parse(Frames.BEACON_1_PAYLOAD, Frames.BEACON_1_HEADER, META_NO_WEP));

        assertTrue(result.isPresent());
        assertTrue(result.get());
    }

    @Test
    public void testMatchesProbeResp() throws MalformedFrameException, IllegalRawDataException {
        BanditIdentifier id = new SSIDIBanditdentifier(new ArrayList<String>() {{
            add("WTF");
            add("foo");
        }}, null, null);

        Optional<Boolean> result = id.matches(new Dot11ProbeResponseFrameParser(new MetricRegistry(), new Anonymizer(false, "")).parse(Frames.PROBE_RESP_2_PAYLOAD, Frames.PROBE_RESP_2_HEADER, META_NO_WEP));

        assertTrue(result.isPresent());
        assertTrue(result.get());
    }

    @Test
    public void testIgnoresBeacon() throws MalformedFrameException, IllegalRawDataException {
        BanditIdentifier id = new SSIDIBanditdentifier(new ArrayList<String>() {{
            add("someSSID");
            add("foo");
        }}, null, null);

        Optional<Boolean> result = id.matches(new Dot11BeaconFrameParser(new MetricRegistry(), new Anonymizer(false, "")).parse(Frames.BEACON_1_PAYLOAD, Frames.BEACON_1_HEADER, META_NO_WEP));

        assertTrue(result.isPresent());
        assertFalse(result.get());
    }

    @Test
    public void testIgnoresProbeResp() throws MalformedFrameException, IllegalRawDataException {
        BanditIdentifier id = new SSIDIBanditdentifier(new ArrayList<String>() {{
            add("someSSID");
            add("foo");
        }}, null, null);

        Optional<Boolean> result = id.matches(new Dot11ProbeResponseFrameParser(new MetricRegistry(), new Anonymizer(false, "")).parse(Frames.PROBE_RESP_1_PAYLOAD, Frames.PROBE_RESP_1_HEADER, META_NO_WEP));

        assertTrue(result.isPresent());
        assertFalse(result.get());
    }

    @Test
    public void testDoesNotRunForDeauth() throws MalformedFrameException, IllegalRawDataException {
        BanditIdentifier id = new SSIDIBanditdentifier(new ArrayList<String>() {{
            add("WTF");
            add("foo");
        }}, null, null);

        Optional<Boolean> result = id.matches(new Dot11DeauthenticationFrameParser(new MetricRegistry(), new Anonymizer(false, "")).parse(Frames.DEAUTH_1_PAYLOAD, Frames.DEAUTH_1_HEADER, META_NO_WEP));

        assertFalse(result.isPresent());
    }

}