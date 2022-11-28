package horse.wtf.nzyme.monitoring.prometheus;

import horse.wtf.nzyme.MockNzyme;
import horse.wtf.nzyme.NzymeLeader;
import horse.wtf.nzyme.dot11.Dot11MetaInformation;
import horse.wtf.nzyme.dot11.MalformedFrameException;
import horse.wtf.nzyme.dot11.anonymization.Anonymizer;
import horse.wtf.nzyme.dot11.frames.Dot11BeaconFrame;
import horse.wtf.nzyme.dot11.parsers.Dot11BeaconFrameParser;
import horse.wtf.nzyme.dot11.parsers.Frames;
import org.testng.annotations.Test;

public class PrometheusFormatterTest {

    protected static final Dot11MetaInformation META_NO_WEP = new Dot11MetaInformation(false, 100, 2400, 1, 0L, false);

    @Test
    public void testFormat() throws Exception, MalformedFrameException {
        NzymeLeader nzyme = new MockNzyme();

        // Create some metrics data.
        Dot11BeaconFrame frame = new Dot11BeaconFrameParser(nzyme.getMetrics(), new Anonymizer(false, ""))
                .parse(Frames.BEACON_1_PAYLOAD, Frames.BEACON_1_HEADER, META_NO_WEP);

        PrometheusFormatter f = new PrometheusFormatter(nzyme);
        System.out.println(f.format());
    }

}