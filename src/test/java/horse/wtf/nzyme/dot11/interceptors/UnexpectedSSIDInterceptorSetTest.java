package horse.wtf.nzyme.dot11.interceptors;

import com.codahale.metrics.MetricRegistry;
import horse.wtf.nzyme.MockNzyme;
import horse.wtf.nzyme.NzymeLeader;
import horse.wtf.nzyme.alerts.Alert;
import horse.wtf.nzyme.alerts.UnexpectedSSIDBeaconAlert;
import horse.wtf.nzyme.alerts.UnexpectedSSIDProbeRespAlert;
import horse.wtf.nzyme.dot11.Dot11FrameInterceptor;
import horse.wtf.nzyme.dot11.Dot11FrameSubtype;
import horse.wtf.nzyme.dot11.MalformedFrameException;
import horse.wtf.nzyme.dot11.parsers.Dot11BeaconFrameParser;
import horse.wtf.nzyme.dot11.parsers.Dot11ProbeResponseFrameParser;
import horse.wtf.nzyme.dot11.parsers.Frames;
import horse.wtf.nzyme.notifications.uplinks.misc.LoopbackUplink;
import org.pcap4j.packet.IllegalRawDataException;
import org.testng.annotations.Test;

import java.util.ArrayList;

import static org.testng.Assert.*;

public class UnexpectedSSIDInterceptorSetTest extends InterceptorSetTest {

    @Test
    public void testGetInterceptors() throws MalformedFrameException, IllegalRawDataException {
        NzymeLeader nzyme = new MockNzyme();
        LoopbackUplink loopback = new LoopbackUplink();
        nzyme.registerUplink(loopback);

        UnexpectedSSIDInterceptorSet set = new UnexpectedSSIDInterceptorSet(nzyme.getAlertsService(), nzyme.getConfiguration().dot11Networks());
        assertEquals(set.getInterceptors().size(), 2);

        for (Dot11FrameInterceptor interceptor : set.getInterceptors()) {
            if (interceptor.forSubtype() == Dot11FrameSubtype.BEACON) {
                assertEquals(interceptor.raisesAlerts(), new ArrayList<Class<? extends Alert>>(){{
                    add(UnexpectedSSIDBeaconAlert.class);
                }});

                // Expected beacon.
                interceptor.intercept(new Dot11BeaconFrameParser(new MetricRegistry()).parse(
                        Frames.BEACON_1_PAYLOAD, Frames.BEACON_1_HEADER, META_NO_WEP
                ));
                assertNull(loopback.getLastAlert());

                // Beacon with a wrong SSID but different BSSID. Should not trigger.
                interceptor.intercept(new Dot11BeaconFrameParser(new MetricRegistry()).parse(
                        Frames.BEACON_3_PAYLOAD, Frames.BEACON_3_HEADER, META_NO_WEP
                ));
                assertNull(loopback.getLastAlert());

                // Unexpected beacon.
                interceptor.intercept(new Dot11BeaconFrameParser(new MetricRegistry()).parse(
                        Frames.BEACON_WTF_WRONG_SSID_PAYLOAD, Frames.BEACON_WTF_WRONG_SSID_HEADER, META_NO_WEP
                ));
                assertNotNull(loopback.getLastAlert());
                assertEquals(UnexpectedSSIDBeaconAlert.class, loopback.getLastAlert().getClass());
            }

            loopback.clear();

            if (interceptor.forSubtype() == Dot11FrameSubtype.PROBE_RESPONSE) {
                assertEquals(interceptor.raisesAlerts(), new ArrayList<Class<? extends Alert>>() {{
                    add(UnexpectedSSIDProbeRespAlert.class);
                }});

                // Expected probe-resp.
                interceptor.intercept(new Dot11ProbeResponseFrameParser(new MetricRegistry()).parse(
                        Frames.PROBE_RESP_3_PAYLOAD, Frames.PROBE_RESP_3_HEADER, META_NO_WEP
                ));
                assertNull(loopback.getLastAlert());

                // Probe-resp with a wrong SSID but different BSSID. Should not trigger.
                interceptor.intercept(new Dot11ProbeResponseFrameParser(new MetricRegistry()).parse(
                        Frames.PROBE_RESP_1_PAYLOAD, Frames.PROBE_RESP_1_HEADER, META_NO_WEP
                ));
                assertNull(loopback.getLastAlert());

                // Unexpected probe-resp.
                interceptor.intercept(new Dot11ProbeResponseFrameParser(new MetricRegistry()).parse(
                        Frames.PROBE_RESP_WTF_WRONG_SSID_PAYLOAD, Frames.PROBE_RESP_WTF_WRONG_SSID_HEADER, META_NO_WEP
                ));
                assertNotNull(loopback.getLastAlert());
                assertEquals(UnexpectedSSIDProbeRespAlert.class, loopback.getLastAlert().getClass());
            }

            loopback.clear();
        }

    }

}