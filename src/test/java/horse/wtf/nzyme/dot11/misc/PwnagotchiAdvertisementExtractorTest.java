package horse.wtf.nzyme.dot11.misc;

import com.codahale.metrics.MetricRegistry;
import horse.wtf.nzyme.dot11.Dot11MetaInformation;
import horse.wtf.nzyme.dot11.MalformedFrameException;
import horse.wtf.nzyme.dot11.anonymization.Anonymizer;
import horse.wtf.nzyme.dot11.frames.Dot11BeaconFrame;
import horse.wtf.nzyme.dot11.interceptors.misc.PwnagotchiAdvertisement;
import horse.wtf.nzyme.dot11.parsers.Dot11BeaconFrameParser;
import horse.wtf.nzyme.dot11.parsers.Frames;
import org.pcap4j.packet.IllegalRawDataException;
import org.testng.annotations.Test;

import java.util.Optional;

import static org.testng.Assert.*;

public class PwnagotchiAdvertisementExtractorTest {

    private static Dot11MetaInformation META = new Dot11MetaInformation(false, 0, 0, 0, 0, false);

    @Test
    public void testExtractAdvertisement() throws MalformedFrameException, IllegalRawDataException {
        PwnagotchiAdvertisementExtractor extractor = new PwnagotchiAdvertisementExtractor();
        Dot11BeaconFrame frame = new Dot11BeaconFrameParser(new MetricRegistry(), new Anonymizer(false, ""))
                .parse(Frames.PWNAGOTCHI_ADVERTISEMENT_BEACON_1_PAYLOAD, Frames.PWNAGOTCHI_ADVERTISEMENT_BEACON_1_HEADER, META);

        Optional<PwnagotchiAdvertisement> extract = extractor.extract(frame);
        assertTrue(extract.isPresent());

        PwnagotchiAdvertisement ad = extract.get();
        assertEquals(ad.name(), "james");
        assertEquals(ad.version(), "1.0.0RC2");
        assertEquals(ad.identity(), "154cc25a09c454a5e5c47e7633bd7cc91091f2d837858d4315e37ba049b869a9");
        assertEquals(ad.uptime(), new Double(265.6851830482483));
        assertEquals(ad.pwndThisRun(),(Integer) 2);
        assertEquals(ad.pwndTotal(), (Integer) 13);
    }

    @Test
    public void testExtractNonAdvertisement() throws MalformedFrameException, IllegalRawDataException {
        PwnagotchiAdvertisementExtractor extractor = new PwnagotchiAdvertisementExtractor();
        Dot11BeaconFrame frame = new Dot11BeaconFrameParser(new MetricRegistry(), new Anonymizer(false, "")).parse(Frames.BEACON_1_PAYLOAD, Frames.BEACON_1_HEADER, META);

        assertFalse(extractor.extract(frame).isPresent());
    }

}