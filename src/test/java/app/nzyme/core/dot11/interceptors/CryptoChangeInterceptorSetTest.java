package app.nzyme.core.dot11.interceptors;

import app.nzyme.core.NzymeNode;
import com.codahale.metrics.MetricRegistry;
import app.nzyme.core.MockNzyme;
import app.nzyme.core.alerts.Alert;
import app.nzyme.core.alerts.CryptoChangeBeaconAlert;
import app.nzyme.core.alerts.CryptoChangeProbeRespAlert;
import app.nzyme.core.dot11.Dot11FrameInterceptor;
import app.nzyme.core.dot11.Dot11FrameSubtype;
import app.nzyme.core.dot11.MalformedFrameException;
import app.nzyme.core.dot11.anonymization.Anonymizer;
import app.nzyme.core.dot11.parsers.Dot11BeaconFrameParser;
import app.nzyme.core.dot11.parsers.Dot11ProbeResponseFrameParser;
import app.nzyme.core.dot11.parsers.Frames;
import app.nzyme.core.notifications.uplinks.misc.LoopbackUplink;
import org.pcap4j.packet.IllegalRawDataException;
import org.testng.annotations.Test;

import java.util.ArrayList;

import static org.testng.Assert.*;

public class CryptoChangeInterceptorSetTest extends InterceptorSetTest {

    @Test
    public void testGetInterceptors() throws MalformedFrameException, IllegalRawDataException {
        NzymeNode nzyme = new MockNzyme();
        LoopbackUplink loopback = new LoopbackUplink();
        nzyme.registerUplink(loopback);

        CryptoChangeInterceptorSet set = new CryptoChangeInterceptorSet(nzyme.getAlertsService(), nzyme.getConfiguration().dot11Networks());
        assertEquals(set.getInterceptors().size(), 2);

        for (Dot11FrameInterceptor interceptor : set.getInterceptors()) {
            reset(loopback, nzyme);
            if (interceptor.forSubtype() == Dot11FrameSubtype.BEACON) {
                assertEquals(interceptor.raisesAlerts(), new ArrayList<Class<? extends Alert>>(){{ add(CryptoChangeBeaconAlert.class); }});

                // Expected beacon.
                interceptor.intercept(new Dot11BeaconFrameParser(new MetricRegistry(), new Anonymizer(false, "")).parse(
                        Frames.BEACON_1_PAYLOAD, Frames.BEACON_1_HEADER, META_NO_WEP
                ));
                assertNull(loopback.getLastAlert());
                reset(loopback, nzyme);

                // Beacon from a different network and different security. Should not trigger.
                interceptor.intercept(new Dot11BeaconFrameParser(new MetricRegistry(), new Anonymizer(false, "")).parse(
                        Frames.BEACON_3_PAYLOAD, Frames.BEACON_3_HEADER, META_NO_WEP
                ));
                assertNull(loopback.getLastAlert());
                reset(loopback, nzyme);

                // Unexpected beacon.
                interceptor.intercept(new Dot11BeaconFrameParser(new MetricRegistry(), new Anonymizer(false, "")).parse(
                        Frames.BEACON_2_PAYLOAD, Frames.BEACON_2_HEADER, META_NO_WEP
                ));
                assertNotNull(loopback.getLastAlert());
                assertEquals(CryptoChangeBeaconAlert.class, loopback.getLastAlert().getClass());
                reset(loopback, nzyme);
            }

            if (interceptor.forSubtype() == Dot11FrameSubtype.PROBE_RESPONSE) {
                assertEquals(interceptor.raisesAlerts(), new ArrayList<Class<? extends Alert>>(){{ add(CryptoChangeProbeRespAlert.class); }});

                // Expected probe-resp.
                interceptor.intercept(new Dot11ProbeResponseFrameParser(new MetricRegistry(), new Anonymizer(false, "")).parse(
                        Frames.PROBE_RESP_3_PAYLOAD, Frames.PROBE_RESP_3_HEADER, META_NO_WEP
                ));
                assertNull(loopback.getLastAlert());
                reset(loopback, nzyme);

                // Probe-resp from a different network and different security. Should not trigger.
                interceptor.intercept(new Dot11ProbeResponseFrameParser(new MetricRegistry(), new Anonymizer(false, "")).parse(
                        Frames.PROBE_RESP_1_PAYLOAD, Frames.PROBE_RESP_1_HEADER, META_NO_WEP
                ));
                assertNull(loopback.getLastAlert());
                reset(loopback, nzyme);

                // Unexpected probe-resp.
                interceptor.intercept(new Dot11ProbeResponseFrameParser(new MetricRegistry(), new Anonymizer(false, "")).parse(
                        Frames.PROBE_RESP_2_PAYLOAD, Frames.PROBE_RESP_2_HEADER, META_NO_WEP
                ));
                assertNotNull(loopback.getLastAlert());
                assertEquals(CryptoChangeProbeRespAlert.class, loopback.getLastAlert().getClass());
                reset(loopback, nzyme);
            }
        }
    }

}