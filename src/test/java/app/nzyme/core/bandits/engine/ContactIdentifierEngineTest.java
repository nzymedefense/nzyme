package app.nzyme.core.bandits.engine;

import com.codahale.metrics.MetricRegistry;
import com.google.common.collect.Lists;
import app.nzyme.core.bandits.Bandit;
import app.nzyme.core.bandits.identifiers.BanditIdentifier;
import app.nzyme.core.bandits.identifiers.FingerprintBanditIdentifier;
import app.nzyme.core.bandits.identifiers.SSIDIBanditdentifier;
import app.nzyme.core.dot11.Dot11MetaInformation;
import app.nzyme.core.dot11.MalformedFrameException;
import app.nzyme.core.dot11.frames.Dot11BeaconFrame;
import app.nzyme.core.dot11.parsers.Dot11BeaconFrameParser;
import app.nzyme.core.dot11.parsers.Frames;
import org.joda.time.DateTime;
import org.pcap4j.packet.IllegalRawDataException;
import org.testng.annotations.Test;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.testng.Assert.*;

public class ContactIdentifierEngineTest {

    @Test
    public void testIdentifyWithBeacon() throws MalformedFrameException, IllegalRawDataException {
        ContactIdentifierEngine engine = new ContactIdentifierEngine(new MetricRegistry());

        List<BanditIdentifier> identifiers = Lists.newArrayList();
        identifiers.add(new SSIDIBanditdentifier(Lists.newArrayList("WTF"), 1L, UUID.randomUUID()));
        Dot11BeaconFrame frame = new Dot11BeaconFrameParser(new MetricRegistry(), null).parse(
                Frames.BEACON_1_PAYLOAD, Frames.BEACON_1_HEADER, new Dot11MetaInformation(false, 0, 0, 1, 0L, false)
        );
        Bandit bandit = Bandit.create(1L, UUID.randomUUID(), "Test", "Test", false, DateTime.now(), DateTime.now(), identifiers);

        Optional<ContactIdentifierEngine.ContactIdentification> result = engine.identify(frame, bandit);

        assertTrue(result.isPresent());
        assertTrue(result.get().ssid().isPresent());
        assertEquals(result.get().ssid().get(), "WTF");
        assertEquals(result.get().bssid(), "00:c0:ca:95:68:3b");
    }

    @Test
    public void testIdentifyWithBroadcastBeacon() throws MalformedFrameException, IllegalRawDataException {
        ContactIdentifierEngine engine = new ContactIdentifierEngine(new MetricRegistry());

        List<BanditIdentifier> identifiers = Lists.newArrayList();
        identifiers.add(new FingerprintBanditIdentifier("52f519b9e8b1a4901a3db02407ff62246f5cfc2d5ddadd5a10e5230524ef04a9", 0L, UUID.randomUUID()));
        Dot11BeaconFrame frame = new Dot11BeaconFrameParser(new MetricRegistry(), null).parse(
                Frames.BEACON_4_PAYLOAD, Frames.BEACON_4_HEADER, new Dot11MetaInformation(false, 0, 0, 1, 0L, false)
        );
        Bandit bandit = Bandit.create(1L, UUID.randomUUID(), "Test", "Test", false, DateTime.now(), DateTime.now(), identifiers);

        Optional<ContactIdentifierEngine.ContactIdentification> result = engine.identify(frame, bandit);

        assertTrue(result.isPresent());
        assertTrue(result.get().ssid().isEmpty());
        assertEquals(result.get().bssid(), "24:a4:3c:7d:01:cc");
    }

    @Test
    public void testIdentifyNoMatch() throws MalformedFrameException, IllegalRawDataException {
        ContactIdentifierEngine engine = new ContactIdentifierEngine(new MetricRegistry());

        List<BanditIdentifier> identifiers = Lists.newArrayList();
        identifiers.add(new SSIDIBanditdentifier(Lists.newArrayList("WTF"), 1L, UUID.randomUUID()));
        Dot11BeaconFrame frame = new Dot11BeaconFrameParser(new MetricRegistry(), null).parse(
                Frames.BEACON_3_PAYLOAD, Frames.BEACON_3_HEADER, new Dot11MetaInformation(false, 0, 0, 1, 0L, false)
        );
        Bandit bandit = Bandit.create(1L, UUID.randomUUID(), "Test", "Test", false, DateTime.now(), DateTime.now(), identifiers);

        Optional<ContactIdentifierEngine.ContactIdentification> result = engine.identify(frame, bandit);

        assertTrue(result.isEmpty());
    }

}