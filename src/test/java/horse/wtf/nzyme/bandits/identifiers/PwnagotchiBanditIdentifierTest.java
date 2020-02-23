package horse.wtf.nzyme.bandits.identifiers;

import com.codahale.metrics.MetricRegistry;
import horse.wtf.nzyme.dot11.MalformedFrameException;
import horse.wtf.nzyme.dot11.parsers.Dot11BeaconFrameParser;
import horse.wtf.nzyme.dot11.parsers.Dot11DeauthenticationFrameParser;
import horse.wtf.nzyme.dot11.parsers.Dot11ProbeResponseFrameParser;
import horse.wtf.nzyme.dot11.parsers.Frames;
import org.pcap4j.packet.IllegalRawDataException;
import org.testng.annotations.Test;

import java.util.Optional;

import static org.testng.Assert.*;

public class PwnagotchiBanditIdentifierTest extends BanditIdentifierTest {

    @Test
    public void testDescriptor() {
        BanditIdentifier id = new PwnagotchiBanditIdentifier("154cc25a09c454a5e5c47e7633bd7cc91091f2d837858d4315e37ba049b869a9", null, null);

        assertEquals(id.descriptor(), BanditIdentifierDescriptor.create(
                BanditIdentifier.TYPE.PWNAGOTCHI_IDENTITY,
                "Matches if the frame is a Pwnagotchi advertisement for the expected Pwnagotchi identity.",
                "frame.pwnagotchi_identity == \"154cc25a09c454a5e5c47e7633bd7cc91091f2d837858d4315e37ba049b869a9\""
        ));
    }

    @Test
    public void testMatchesBeacon() throws MalformedFrameException, IllegalRawDataException {
        BanditIdentifier id = new PwnagotchiBanditIdentifier("154cc25a09c454a5e5c47e7633bd7cc91091f2d837858d4315e37ba049b869a9", null, null);
        Optional<Boolean> result = id.matches(new Dot11BeaconFrameParser(new MetricRegistry()).parse(Frames.PWNAGOTCHI_ADVERTISEMENT_BEACON_1_PAYLOAD, Frames.PWNAGOTCHI_ADVERTISEMENT_BEACON_1_HEADER, META_NO_WEP));

        assertTrue(result.isPresent());
        assertTrue(result.get());
    }

    @Test
    public void testIgnoresBeacon() throws MalformedFrameException, IllegalRawDataException {
        BanditIdentifier id = new PwnagotchiBanditIdentifier("154cc25a09c454a5e5c47e7633bd7cc91091f2d837858d4315e37ba049b869a9", null, null);
        Optional<Boolean> result = id.matches(new Dot11BeaconFrameParser(new MetricRegistry()).parse(Frames.BEACON_1_PAYLOAD, Frames.BEACON_1_HEADER, META_NO_WEP));

        assertTrue(result.isPresent());
        assertFalse(result.get());
    }

    @Test
    public void testIgnoresProbeResp() throws MalformedFrameException, IllegalRawDataException {
        BanditIdentifier id = new PwnagotchiBanditIdentifier("154cc25a09c454a5e5c47e7633bd7cc91091f2d837858d4315e37ba049b869a9", null, null);
        Optional<Boolean> result = id.matches(new Dot11ProbeResponseFrameParser(new MetricRegistry()).parse(Frames.PROBE_RESP_1_PAYLOAD, Frames.PROBE_RESP_1_HEADER, META_NO_WEP));

        assertFalse(result.isPresent());
    }

    @Test
    public void testIgnoresDeauth() throws MalformedFrameException, IllegalRawDataException {
        BanditIdentifier id = new PwnagotchiBanditIdentifier("154cc25a09c454a5e5c47e7633bd7cc91091f2d837858d4315e37ba049b869a9", null, null);
        Optional<Boolean> result = id.matches(new Dot11DeauthenticationFrameParser(new MetricRegistry()).parse(Frames.DEAUTH_1_PAYLOAD, Frames.DEAUTH_1_HEADER, META_NO_WEP));

        assertFalse(result.isPresent());
    }
}