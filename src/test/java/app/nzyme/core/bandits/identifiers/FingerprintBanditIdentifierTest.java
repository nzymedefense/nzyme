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

import java.util.Optional;

import static org.testng.Assert.*;

public class FingerprintBanditIdentifierTest extends BanditIdentifierTest {

    @Test
    public void testDescriptor() {
        BanditIdentifier id = new FingerprintBanditIdentifier("dfac3abce0c722f9609343f7dfa208afa51a1c7decbd2eb6f96c78051f0a594b", null, null);

        assertEquals(id.descriptor(), BanditIdentifierDescriptor.create(
                BanditIdentifier.TYPE.FINGERPRINT,
                "Matches if the frame fingerprint equals the expected fingerprint.",
                "frame.fingerprint == \"dfac3abce0c722f9609343f7dfa208afa51a1c7decbd2eb6f96c78051f0a594b\""
        ));
    }

    @Test
    public void testMatchesBeacon() throws MalformedFrameException, IllegalRawDataException {
        BanditIdentifier id = new FingerprintBanditIdentifier("dfac3abce0c722f9609343f7dfa208afa51a1c7decbd2eb6f96c78051f0a594b", null, null);

        Optional<Boolean> result = id.matches(new Dot11BeaconFrameParser(new MetricRegistry(), new Anonymizer(false, "")).parse(Frames.BEACON_1_PAYLOAD, Frames.BEACON_1_HEADER, META_NO_WEP));

        assertTrue(result.isPresent());
        assertTrue(result.get());
    }

    @Test
    public void testMatchesProbeResp() throws MalformedFrameException, IllegalRawDataException {
        BanditIdentifier id = new FingerprintBanditIdentifier("2187f729bf5093a1347acac583c86a1ed72c5ce2df0ed2628cafcee4e78e591d", null, null);

        Optional<Boolean> result = id.matches(new Dot11ProbeResponseFrameParser(new MetricRegistry(), new Anonymizer(false, "")).parse(Frames.PROBE_RESP_1_PAYLOAD, Frames.PROBE_RESP_1_HEADER, META_NO_WEP));

        assertTrue(result.isPresent());
        assertTrue(result.get());
    }

    @Test
    public void testIgnoresBeacon() throws MalformedFrameException, IllegalRawDataException {
        BanditIdentifier id = new FingerprintBanditIdentifier("dfac3abce0c722f9609343f7dfa208afa51a1c7decbd2eb6f96c78051f0a594b", null, null);

        Optional<Boolean> result = id.matches(new Dot11BeaconFrameParser(new MetricRegistry(), new Anonymizer(false, "")).parse(Frames.BEACON_2_PAYLOAD, Frames.BEACON_2_HEADER, META_NO_WEP));

        assertTrue(result.isPresent());
        assertFalse(result.get());
    }

    @Test
    public void testIgnoresProbeResp() throws MalformedFrameException, IllegalRawDataException {
        BanditIdentifier id = new FingerprintBanditIdentifier("6586b438a7ef3c680c39983b8f2a079e53962f12b302f5ffeeaf4daad2e8ca33", null, null);

        Optional<Boolean> result = id.matches(new Dot11ProbeResponseFrameParser(new MetricRegistry(), new Anonymizer(false, "")).parse(Frames.PROBE_RESP_1_PAYLOAD, Frames.PROBE_RESP_1_HEADER, META_NO_WEP));

        assertTrue(result.isPresent());
        assertFalse(result.get());
    }

    @Test
    public void testDoesNotRunForDeauth() throws MalformedFrameException, IllegalRawDataException {
        BanditIdentifier id = new FingerprintBanditIdentifier("foo", null, null);

        Optional<Boolean> result = id.matches(new Dot11DeauthenticationFrameParser(new MetricRegistry(), new Anonymizer(false, "")).parse(Frames.DEAUTH_1_PAYLOAD, Frames.DEAUTH_1_HEADER, META_NO_WEP));

        assertFalse(result.isPresent());
    }

}