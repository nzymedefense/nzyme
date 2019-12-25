package horse.wtf.nzyme.bandits.identifiers;

import com.codahale.metrics.MetricRegistry;
import horse.wtf.nzyme.dot11.Dot11MetaInformation;
import horse.wtf.nzyme.dot11.MalformedFrameException;
import horse.wtf.nzyme.dot11.parsers.Dot11BeaconFrameParser;
import horse.wtf.nzyme.dot11.parsers.Dot11DeauthenticationFrameParser;
import horse.wtf.nzyme.dot11.parsers.Dot11ProbeResponseFrameParser;
import horse.wtf.nzyme.dot11.parsers.Frames;
import org.pcap4j.packet.IllegalRawDataException;
import org.testng.annotations.Test;

import java.util.ArrayList;
import java.util.Optional;

import static org.testng.Assert.*;

public class SignalStrengthBanditIdentifierTest {

    @Test(expectedExceptions = {IllegalArgumentException.class})
    public void testDoesNotAllowOutOfRangeFromValue() {
        new SignalStrengthBanditIdentifier(10, -50);
    }

    @Test(expectedExceptions = {IllegalArgumentException.class})
    public void testDoesNotAllowOutOfRangeToValue() {
        new SignalStrengthBanditIdentifier(-15, -110);
    }

    @Test(expectedExceptions = {IllegalArgumentException.class})
    public void testDoesNotAllowFromValueLowerThanToValue() {
        new SignalStrengthBanditIdentifier(-50, -15);
    }

    @Test
    public void testDescriptor() {
        BanditIdentifier id = new SignalStrengthBanditIdentifier(-15, -50);

        assertEquals(id.descriptor(), BanditIdentifier.Descriptor.create(
                BanditIdentifier.TYPE.SIGNAL_STRENGTH,
                "Matches if the frame signal strength is within expected range.",
                "(frame.signal_quality >= -50 AND frame.signal_quality <= -15)"
        ));
    }

    @Test
    public void testMatchesBeacon() throws MalformedFrameException, IllegalRawDataException {
        BanditIdentifier id = new SignalStrengthBanditIdentifier(-15, -50);

        Optional<Boolean> result = id.matches(new Dot11BeaconFrameParser(new MetricRegistry()).parse(Frames.BEACON_1_PAYLOAD, Frames.BEACON_1_HEADER, signal(-35)));

        assertTrue(result.isPresent());
        assertTrue(result.get());
    }

    @Test
    public void testMatchesProbeResp() throws MalformedFrameException, IllegalRawDataException {
        BanditIdentifier id = new SignalStrengthBanditIdentifier(-15, -50);

        Optional<Boolean> result = id.matches(new Dot11ProbeResponseFrameParser(new MetricRegistry()).parse(Frames.PROBE_RESP_1_PAYLOAD, Frames.PROBE_RESP_1_HEADER, signal(-35)));

        assertTrue(result.isPresent());
        assertTrue(result.get());
    }

    @Test
    public void testMatchesDeauth() throws MalformedFrameException, IllegalRawDataException {
        BanditIdentifier id = new SignalStrengthBanditIdentifier(-15, -50);

        Optional<Boolean> result = id.matches(new Dot11DeauthenticationFrameParser(new MetricRegistry()).parse(Frames.DEAUTH_1_PAYLOAD, Frames.DEAUTH_1_HEADER, signal(-35)));

        assertTrue(result.isPresent());
        assertTrue(result.get());
    }

    @Test
    public void testIgnoresBeacon() throws MalformedFrameException, IllegalRawDataException {
        BanditIdentifier id = new SignalStrengthBanditIdentifier(-15, -50);

        Optional<Boolean> result = id.matches(new Dot11BeaconFrameParser(new MetricRegistry()).parse(Frames.BEACON_1_PAYLOAD, Frames.BEACON_1_HEADER, signal(-55)));

        assertTrue(result.isPresent());
        assertFalse(result.get());
    }

    @Test
    public void testIgnoresProbeResp() throws MalformedFrameException, IllegalRawDataException {
        BanditIdentifier id = new SignalStrengthBanditIdentifier(-15, -50);

        Optional<Boolean> result = id.matches(new Dot11ProbeResponseFrameParser(new MetricRegistry()).parse(Frames.PROBE_RESP_1_PAYLOAD, Frames.PROBE_RESP_1_HEADER, signal(-55)));

        assertTrue(result.isPresent());
        assertFalse(result.get());
    }

    @Test
    public void testIgnoresDeauth() throws MalformedFrameException, IllegalRawDataException {
        BanditIdentifier id = new SignalStrengthBanditIdentifier(-15, -50);

        Optional<Boolean> result = id.matches(new Dot11DeauthenticationFrameParser(new MetricRegistry()).parse(Frames.DEAUTH_1_PAYLOAD, Frames.DEAUTH_1_HEADER, signal(-55)));

        assertTrue(result.isPresent());
        assertFalse(result.get());
    }

    private Dot11MetaInformation signal(int antennaSignal) {
        return new Dot11MetaInformation(false, antennaSignal, 9001, 11, 0L, false);
    }

}