package horse.wtf.nzyme.dot11.interceptors;

import com.codahale.metrics.MetricRegistry;
import horse.wtf.nzyme.MockNzyme;
import horse.wtf.nzyme.Nzyme;
import horse.wtf.nzyme.alerts.Alert;
import horse.wtf.nzyme.alerts.KnownBanditFingerprintBeaconAlert;
import horse.wtf.nzyme.alerts.KnownBanditFingerprintProbeRespAlert;
import horse.wtf.nzyme.dot11.Dot11FrameInterceptor;
import horse.wtf.nzyme.dot11.Dot11FrameSubtype;
import horse.wtf.nzyme.dot11.MalformedFrameException;
import horse.wtf.nzyme.dot11.parsers.Dot11BeaconFrameParser;
import horse.wtf.nzyme.dot11.parsers.Dot11ProbeResponseFrameParser;
import horse.wtf.nzyme.dot11.parsers.Frames;
import horse.wtf.nzyme.dot11.probes.Dot11MockProbe;
import horse.wtf.nzyme.notifications.uplinks.misc.LoopbackUplink;
import org.pcap4j.packet.IllegalRawDataException;
import org.testng.annotations.Test;

import java.util.ArrayList;

import static org.testng.Assert.*;

public class KnownBanditFingerprintInterceptorSetTest extends InterceptorSetTest {

    @Test
    public void testGetInterceptors() throws MalformedFrameException, IllegalRawDataException {
        Nzyme nzyme = new MockNzyme();
        Dot11MockProbe probe = buildMockProbe(nzyme);
        LoopbackUplink loopback = new LoopbackUplink();
        nzyme.registerUplink(loopback);

        KnownBanditFingerprintInterceptorSet set = new KnownBanditFingerprintInterceptorSet(probe);
        assertEquals(set.getInterceptors().size(), 2);

        for (Dot11FrameInterceptor interceptor : set.getInterceptors()) {
            if (interceptor.forSubtype() == Dot11FrameSubtype.BEACON) {
                assertEquals(interceptor.raisesAlerts(), new ArrayList<Class<? extends Alert>>(){{ add(KnownBanditFingerprintBeaconAlert.class); }});

                interceptor.intercept(new Dot11BeaconFrameParser(new MetricRegistry()).parse(
                        Frames.BEACON_1_PAYLOAD, Frames.BEACON_1_HEADER, META_NO_WEP
                ));
                assertNull(loopback.getLastAlert());

                interceptor.intercept(new Dot11BeaconFrameParser(new MetricRegistry()).parse(
                        Frames.BEACON_PINEAPPLE_NANO_2_5_2_PINEAP_PAYLOAD, Frames.BEACON_PINEAPPLE_NANO_2_5_2_PINEAP_HEADER, META_NO_WEP
                ));
                assertNotNull(loopback.getLastAlert());
                assertEquals(KnownBanditFingerprintBeaconAlert.class, loopback.getLastAlert().getClass());
            }

            loopback.clear();

            if (interceptor.forSubtype() == Dot11FrameSubtype.PROBE_RESPONSE) {
                assertEquals(interceptor.raisesAlerts(), new ArrayList<Class<? extends Alert>>(){{ add(KnownBanditFingerprintProbeRespAlert.class); }});

                interceptor.intercept(new Dot11ProbeResponseFrameParser(new MetricRegistry()).parse(
                        Frames.PROBE_RESP_1_PAYLOAD, Frames.PROBE_RESP_1_HEADER, META_NO_WEP
                ));
                assertNull(loopback.getLastAlert());

                interceptor.intercept(new Dot11ProbeResponseFrameParser(new MetricRegistry()).parse(
                        Frames.PROBE_RESP_PINEAPPLE_NANO_2_5_2_PINEAP_PAYLOAD, Frames.PROBE_RESP_PINEAPPLE_NANO_2_5_2_PINEAP_HEADER, META_NO_WEP
                ));
                assertNotNull(loopback.getLastAlert());
                assertEquals(KnownBanditFingerprintProbeRespAlert.class, loopback.getLastAlert().getClass());
            }

            loopback.clear();
        }
    }

}