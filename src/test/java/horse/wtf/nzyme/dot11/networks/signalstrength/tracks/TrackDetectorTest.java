package horse.wtf.nzyme.dot11.networks.signalstrength.tracks;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.joda.JodaModule;
import horse.wtf.nzyme.ResourcesAccessingTest;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.testng.annotations.Test;

import java.io.File;
import java.io.IOException;
import java.util.List;

import static org.testng.Assert.*;

public class TrackDetectorTest extends ResourcesAccessingTest {

    private static final Logger LOG = LogManager.getLogger(TrackDetectorTest.class);

    private SignalWaterfallHistogram loadHistogram(String resourcePath) {
        File json = loadFromResourceFile(resourcePath);

        try {
            ObjectMapper om = new ObjectMapper();
            om.registerModule(new JodaModule());
            return om.readValue(json, SignalWaterfallHistogram.class);
        } catch (IOException e) {
            fail("Couldn't map resource waterfall file to object.", e);
            return null;
        }
    }

    @Test
    public void testDetectScenario001() {
        TrackDetector detector = new TrackDetector(loadHistogram("tracks/001_8h_single_10db_spread.json"));
        List<Track> tracks = detector.detect(TrackDetector.DEFAULT_CONFIG);

        // Can't compare lists directly because of JUnit DateTime timezone weirdness.
        assertEquals(tracks.size(), 1);
        Track one = tracks.get(0);

        assertEquals(one.start().withZone(DateTimeZone.UTC), new DateTime("2019-11-02T15:24:00.851Z").withZone(DateTimeZone.UTC));
        assertEquals(one.end().withZone(DateTimeZone.UTC), new DateTime("2019-11-02T19:22:00.321Z").withZone(DateTimeZone.UTC));
        assertEquals(one.centerline(), -43);
        assertEquals(one.minSignal(), -49);
        assertEquals(one.maxSignal(), -40);
    }

    @Test
    public void testDetectScenario002() {
        TrackDetector detector = new TrackDetector(loadHistogram("tracks/002_4h_two_pineapple.json"));
        List<Track> tracks = detector.detect(TrackDetector.DEFAULT_CONFIG);

        // Can't compare lists directly because of JUnit DateTime timezone weirdness.
        assertEquals(tracks.size(), 3);
        Track one = tracks.get(0);
        Track two = tracks.get(1);
        Track three = tracks.get(2);

        assertEquals(one.start().withZone(DateTimeZone.UTC), new DateTime("2019-11-02T22:28:00.466Z").withZone(DateTimeZone.UTC));
        assertEquals(one.end().withZone(DateTimeZone.UTC), new DateTime("2019-11-02T22:33:00.734Z").withZone(DateTimeZone.UTC));
        assertEquals(one.centerline(), -17);
        assertEquals(one.minSignal(), -19);
        assertEquals(one.maxSignal(), -16);

        assertEquals(two.start().withZone(DateTimeZone.UTC), new DateTime("2019-11-02T20:05:00.309Z").withZone(DateTimeZone.UTC));
        assertEquals(two.end().withZone(DateTimeZone.UTC), new DateTime("2019-11-03T00:04:00.105Z").withZone(DateTimeZone.UTC));
        assertEquals(two.centerline(), -66);
        assertEquals(two.minSignal(), -73);
        assertEquals(two.maxSignal(), -65);

        assertEquals(three.start().withZone(DateTimeZone.UTC), new DateTime("2019-11-02T22:29:00.576Z").withZone(DateTimeZone.UTC));
        assertEquals(three.end().withZone(DateTimeZone.UTC), new DateTime("2019-11-02T23:26:00.900Z").withZone(DateTimeZone.UTC));
        assertEquals(three.centerline(), -41);
        assertEquals(three.minSignal(), -49);
        assertEquals(three.maxSignal(), -39);
    }

}