package horse.wtf.nzyme.bandits.engine;

import com.codahale.metrics.MetricRegistry;
import com.google.common.collect.Lists;
import horse.wtf.nzyme.bandits.Bandit;
import horse.wtf.nzyme.bandits.identifiers.BanditIdentifier;
import horse.wtf.nzyme.bandits.identifiers.FingerprintBanditIdentifier;
import horse.wtf.nzyme.bandits.identifiers.SSIDIBanditdentifier;
import horse.wtf.nzyme.dot11.Dot11MetaInformation;
import horse.wtf.nzyme.dot11.MalformedFrameException;
import horse.wtf.nzyme.dot11.anonymization.Anonymizer;
import horse.wtf.nzyme.dot11.frames.Dot11BeaconFrame;
import horse.wtf.nzyme.dot11.parsers.Dot11BeaconFrameParser;
import horse.wtf.nzyme.dot11.parsers.Frames;
import org.joda.time.DateTime;
import org.pcap4j.packet.IllegalRawDataException;
import org.testng.annotations.Test;

import java.util.List;
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

        ContactIdentifierEngine.ContactIdentifierResult result = engine.identify(frame, bandit);

        assertTrue(result.match());
        assertTrue(result.ssid().isPresent());
        assertEquals(result.ssid().get(), "WTF");
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

        ContactIdentifierEngine.ContactIdentifierResult result = engine.identify(frame, bandit);

        assertTrue(result.match());
        assertTrue(result.ssid().isEmpty());
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

        ContactIdentifierEngine.ContactIdentifierResult result = engine.identify(frame, bandit);

        assertFalse(result.match());
        assertTrue(result.ssid().isEmpty());
    }

}