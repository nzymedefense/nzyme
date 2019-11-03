package horse.wtf.nzyme.dot11.networks.signalstrength.tracks;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.joda.JodaModule;
import horse.wtf.nzyme.ResourcesAccessingTest;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.testng.annotations.Test;

import java.io.File;
import java.io.IOException;

import static org.testng.Assert.*;

public class TrackDetectorTest extends ResourcesAccessingTest {

    private static final Logger LOG = LogManager.getLogger(TrackDetectorTest.class);

    @Test
    public void testDetect() {
        File json = loadFromResourceFile("tracks/002_4h_two_pineapple.json");

        SignalWaterfallHistogram histogram = null;
        try {
            ObjectMapper om = new ObjectMapper();
            om.registerModule(new JodaModule());
            histogram = om.readValue(json, SignalWaterfallHistogram.class);
        } catch (IOException e) {
            fail("Couldn't map resource waterfall file to object.", e);
        }

        TrackDetector detector = new TrackDetector(histogram);
        LOG.info(detector.detect());
    }

}