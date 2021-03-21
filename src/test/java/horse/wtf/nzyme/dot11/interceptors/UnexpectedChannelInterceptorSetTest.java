package horse.wtf.nzyme.dot11.interceptors;

import com.codahale.metrics.MetricRegistry;
import horse.wtf.nzyme.MockNzyme;
import horse.wtf.nzyme.NzymeLeader;
import horse.wtf.nzyme.alerts.Alert;
import horse.wtf.nzyme.alerts.UnexpectedChannelBeaconAlert;
import horse.wtf.nzyme.alerts.UnexpectedChannelProbeRespAlert;
import horse.wtf.nzyme.dot11.Dot11FrameInterceptor;
import horse.wtf.nzyme.dot11.Dot11FrameSubtype;
import horse.wtf.nzyme.dot11.Dot11MetaInformation;
import horse.wtf.nzyme.dot11.MalformedFrameException;
import horse.wtf.nzyme.dot11.anonymization.Anonymizer;
import horse.wtf.nzyme.dot11.parsers.Dot11BeaconFrameParser;
import horse.wtf.nzyme.dot11.parsers.Dot11ProbeResponseFrameParser;
import horse.wtf.nzyme.dot11.parsers.Frames;
import horse.wtf.nzyme.notifications.uplinks.misc.LoopbackUplink;
import org.pcap4j.packet.IllegalRawDataException;
import org.testng.annotations.Test;

import java.util.ArrayList;

import static org.testng.Assert.*;

public class UnexpectedChannelInterceptorSetTest extends InterceptorSetTest {

    protected static final Dot11MetaInformation META_NO_WEP_CHANNEL_3 = new Dot11MetaInformation(false, 100, 2400, 3, 0L, false);

    @Test
    public void testGetInterceptors() throws MalformedFrameException, IllegalRawDataException {
        NzymeLeader nzyme = new MockNzyme();
        LoopbackUplink loopback = new LoopbackUplink();
        nzyme.registerUplink(loopback);

        UnexpectedChannelInterceptorSet set = new UnexpectedChannelInterceptorSet(nzyme.getAlertsService(), nzyme.getConfiguration().dot11Networks());
        assertEquals(set.getInterceptors().size(), 2);

        for (Dot11FrameInterceptor interceptor : set.getInterceptors()) {
            reset(loopback, nzyme);
            if (interceptor.forSubtype() == Dot11FrameSubtype.BEACON) {
                assertEquals(interceptor.raisesAlerts(), new ArrayList<Class<? extends Alert>>() {{
                    add(UnexpectedChannelBeaconAlert.class);
                }});

                // Expected beacon.
                interceptor.intercept(new Dot11BeaconFrameParser(new MetricRegistry(), new Anonymizer(false, "")).parse(
                        Frames.BEACON_1_PAYLOAD, Frames.BEACON_1_HEADER, META_NO_WEP
                ));
                assertNull(loopback.getLastAlert());
                reset(loopback, nzyme);

                // Beacon from a wrong channel but different network. Should not trigger.
                interceptor.intercept(new Dot11BeaconFrameParser(new MetricRegistry(), new Anonymizer(false, "")).parse(
                        Frames.BEACON_3_PAYLOAD, Frames.BEACON_3_HEADER, META_NO_WEP_CHANNEL_3
                ));
                assertNull(loopback.getLastAlert());
                reset(loopback, nzyme);

                // Unexpected beacon.
                interceptor.intercept(new Dot11BeaconFrameParser(new MetricRegistry(), new Anonymizer(false, "")).parse(
                        Frames.BEACON_1_PAYLOAD, Frames.BEACON_1_HEADER, META_NO_WEP_CHANNEL_3
                ));
                assertNotNull(loopback.getLastAlert());
                assertEquals(UnexpectedChannelBeaconAlert.class, loopback.getLastAlert().getClass());
                reset(loopback, nzyme);
            }

            if (interceptor.forSubtype() == Dot11FrameSubtype.PROBE_RESPONSE) {
                assertEquals(interceptor.raisesAlerts(), new ArrayList<Class<? extends Alert>>() {{
                    add(UnexpectedChannelProbeRespAlert.class);
                }});

                // Expected probe-resp.
                interceptor.intercept(new Dot11ProbeResponseFrameParser(new MetricRegistry(), new Anonymizer(false, "")).parse(
                        Frames.PROBE_RESP_2_PAYLOAD, Frames.PROBE_RESP_3_HEADER, META_NO_WEP
                ));
                assertNull(loopback.getLastAlert());
                reset(loopback, nzyme);

                // Probe-resp from a wrong channel but different network. Should not trigger.
                interceptor.intercept(new Dot11ProbeResponseFrameParser(new MetricRegistry(), new Anonymizer(false, "")).parse(
                        Frames.PROBE_RESP_1_PAYLOAD, Frames.PROBE_RESP_1_HEADER, META_NO_WEP_CHANNEL_3
                ));
                assertNull(loopback.getLastAlert());
                reset(loopback, nzyme);

                // Unexpected probe-resp.
                interceptor.intercept(new Dot11ProbeResponseFrameParser(new MetricRegistry(), new Anonymizer(false, "")).parse(
                        Frames.PROBE_RESP_2_PAYLOAD, Frames.PROBE_RESP_2_HEADER, META_NO_WEP_CHANNEL_3
                ));
                assertNotNull(loopback.getLastAlert());
                assertEquals(UnexpectedChannelProbeRespAlert.class, loopback.getLastAlert().getClass());
                reset(loopback, nzyme);
            }
        }
    }

}