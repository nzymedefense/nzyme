package horse.wtf.nzyme.dot11.interceptors;

import com.codahale.metrics.MetricRegistry;
import horse.wtf.nzyme.MockNzyme;
import horse.wtf.nzyme.NzymeLeader;
import horse.wtf.nzyme.alerts.Alert;
import horse.wtf.nzyme.alerts.UnexpectedFingerprintBeaconAlert;
import horse.wtf.nzyme.alerts.UnexpectedFingerprintProbeRespAlert;
import horse.wtf.nzyme.dot11.Dot11FrameInterceptor;
import horse.wtf.nzyme.dot11.Dot11FrameSubtype;
import horse.wtf.nzyme.dot11.Dot11TaggedParameters;
import horse.wtf.nzyme.dot11.MalformedFrameException;
import horse.wtf.nzyme.dot11.anonymization.Anonymizer;
import horse.wtf.nzyme.dot11.frames.Dot11ProbeResponseFrame;
import horse.wtf.nzyme.dot11.parsers.Dot11BeaconFrameParser;
import horse.wtf.nzyme.dot11.parsers.Frames;
import horse.wtf.nzyme.notifications.uplinks.misc.LoopbackUplink;
import org.pcap4j.packet.IllegalRawDataException;
import org.testng.annotations.Test;

import java.util.ArrayList;

import static org.testng.Assert.*;

public class UnexpectedFingerprintInterceptorSetTest extends InterceptorSetTest {

    @Test
    public void testGetInterceptors() throws MalformedFrameException, IllegalRawDataException {
        NzymeLeader nzyme = new MockNzyme();
        LoopbackUplink loopback = new LoopbackUplink();
        nzyme.registerUplink(loopback);

        UnexpectedFingerprintInterceptorSet set = new UnexpectedFingerprintInterceptorSet(nzyme.getAlertsService(), nzyme.getConfiguration().dot11Networks());
        assertEquals(set.getInterceptors().size(), 2);

        for (Dot11FrameInterceptor interceptor : set.getInterceptors()) {
            reset(loopback, nzyme);
            if (interceptor.forSubtype() == Dot11FrameSubtype.BEACON) {
                assertEquals(interceptor.raisesAlerts(), new ArrayList<Class<? extends Alert>>(){{
                    add(UnexpectedFingerprintBeaconAlert.class);
                }});

                // Expected beacon.
                interceptor.intercept(new Dot11BeaconFrameParser(new MetricRegistry(), new Anonymizer(false, "")).parse(
                        Frames.BEACON_1_PAYLOAD, Frames.BEACON_1_HEADER, META_NO_WEP
                ));
                assertNull(loopback.getLastAlert());
                reset(loopback, nzyme);

                // Beacon with a wrong fingerprint but different BSSID. Should not trigger.
                interceptor.intercept(new Dot11BeaconFrameParser(new MetricRegistry(), new Anonymizer(false, "")).parse(
                        Frames.BEACON_3_PAYLOAD, Frames.BEACON_3_HEADER, META_NO_WEP
                ));
                assertNull(loopback.getLastAlert());
                reset(loopback, nzyme);

                // TODO: Unexpected fingerprint.
                interceptor.intercept(new Dot11BeaconFrameParser(new MetricRegistry(), new Anonymizer(false, "")).parse(
                        Frames.BEACON_2_PAYLOAD, Frames.BEACON_2_PAYLOAD, META_NO_WEP
                ));
                assertNotNull(loopback.getLastAlert());
                assertEquals(UnexpectedFingerprintBeaconAlert.class, loopback.getLastAlert().getClass());
                reset(loopback, nzyme);
            }

            if (interceptor.forSubtype() == Dot11FrameSubtype.PROBE_RESPONSE) {
                assertEquals(interceptor.raisesAlerts(), new ArrayList<Class<? extends Alert>>() {{
                    add(UnexpectedFingerprintProbeRespAlert.class);
                }});

                // TODO: Don't have appropriate frames in library so creating them directly for this part of the test.

                // Expected probe-resp.
                interceptor.intercept(Dot11ProbeResponseFrame.create(
                        "WTF",
                        "ff:ff:ff:ff:ff:ff",
                        "00:c0:ca:95:68:3b",
                        "dfac3abce0c722f9609343f7dfa208afa51a1c7decbd2eb6f96c78051f0a594b",
                        new Dot11TaggedParameters(new MetricRegistry(), Dot11TaggedParameters.PROBERESP_TAGGED_PARAMS_POSITION, Frames.PROBE_RESP_1_PAYLOAD),
                        META_NO_WEP)
                );
                assertNull(loopback.getLastAlert());
                reset(loopback, nzyme);

                // Probe-resp with a wrong fingerprint but different BSSID. Should not trigger.
                interceptor.intercept(Dot11ProbeResponseFrame.create(
                        "WTF",
                        "ff:ff:ff:ff:ff:ff",
                        "0a:c0:ca:95:68:3b",
                        "WRONGdfac3abce0c722f9609343f7dfa208afa51a1c7decbd2eb6f96c78051f0a594b",
                        new Dot11TaggedParameters(new MetricRegistry(), Dot11TaggedParameters.PROBERESP_TAGGED_PARAMS_POSITION, Frames.PROBE_RESP_1_PAYLOAD),
                        META_NO_WEP)
                );
                assertNull(loopback.getLastAlert());
                reset(loopback, nzyme);

                // Unexpected fingerprint.
                interceptor.intercept(Dot11ProbeResponseFrame.create(
                        "WTF",
                        "ff:ff:ff:ff:ff:ff",
                        "00:c0:ca:95:68:3b",
                        "WRONGdfac3abce0c722f9609343f7dfa208afa51a1c7decbd2eb6f96c78051f0a594b",
                        new Dot11TaggedParameters(new MetricRegistry(), Dot11TaggedParameters.PROBERESP_TAGGED_PARAMS_POSITION, Frames.PROBE_RESP_1_PAYLOAD),
                        META_NO_WEP)
                );
                assertNotNull(loopback.getLastAlert());
                assertEquals(UnexpectedFingerprintProbeRespAlert.class, loopback.getLastAlert().getClass());
                reset(loopback, nzyme);
            }
        }

    }

}