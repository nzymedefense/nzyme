package horse.wtf.nzyme.dot11.interceptors;

import com.codahale.metrics.MetricRegistry;
import horse.wtf.nzyme.MockNzyme;
import horse.wtf.nzyme.dot11.Dot11FrameInterceptor;
import horse.wtf.nzyme.dot11.Dot11FrameSubtype;
import horse.wtf.nzyme.dot11.MalformedFrameException;
import horse.wtf.nzyme.dot11.parsers.*;
import horse.wtf.nzyme.dot11.probes.Dot11MockProbe;
import horse.wtf.nzyme.notifications.uplinks.misc.LoopbackUplink;
import org.pcap4j.packet.IllegalRawDataException;
import org.testng.annotations.Test;

import static org.testng.Assert.*;

public class NetworksAndClientsInterceptorSetTest extends InterceptorSetTest {

    @Test
    public void testGetInterceptors() throws MalformedFrameException, IllegalRawDataException {
        MockNzyme nzyme = new MockNzyme();
        Dot11MockProbe probe = buildMockProbe(BANDITS_STANDARD, nzyme);
        LoopbackUplink loopback = new LoopbackUplink();
        probe.registerUplink(loopback);

        NetworksAndClientsInterceptorSet set = new NetworksAndClientsInterceptorSet(nzyme);
        assertEquals(set.getInterceptors().size(), 4);

        for (Dot11FrameInterceptor interceptor : set.getInterceptors()) {
            if (interceptor.forSubtype() == Dot11FrameSubtype.BEACON) {
                interceptor.intercept(new Dot11BeaconFrameParser(new MetricRegistry()).parse(
                        Frames.BEACON_1_PAYLOAD, Frames.BEACON_1_HEADER, META_NO_WEP
                ));
            }

            if (interceptor.forSubtype() == Dot11FrameSubtype.PROBE_RESPONSE) {
                interceptor.intercept(new Dot11ProbeResponseFrameParser(new MetricRegistry()).parse(
                        Frames.PROBE_RESP_1_PAYLOAD, Frames.PROBE_RESP_1_HEADER, META_NO_WEP
                ));
            }

            if (interceptor.forSubtype() == Dot11FrameSubtype.PROBE_REQUEST) {
                interceptor.intercept(new Dot11ProbeRequestFrameParser(new MetricRegistry()).parse(
                        Frames.PROBE_REQ_1_PAYLOAD, Frames.PROBE_REQ_1_HEADER, META_NO_WEP
                ));
            }

            if (interceptor.forSubtype() == Dot11FrameSubtype.ASSOCIATION_REQUEST) {
                interceptor.intercept(new Dot11AssociationRequestFrameParser(new MetricRegistry()).parse(
                        Frames.ASSOC_REQ_1_PAYLOAD, Frames.ASSOC_REQ_1_HEADER, META_NO_WEP
                ));
            }
        }

        assertEquals(nzyme.getNetworks().getSSIDs().size(), 2);

        assertEquals(nzyme.getNetworks().getBSSIDs().size(), 2);
        assertNotNull(nzyme.getNetworks().getBSSIDs().get("b0:93:5b:1d:c8:f1"));
        assertNotNull(nzyme.getNetworks().getBSSIDs().get("00:c0:ca:95:68:3b"));

        assertEquals(nzyme.getClients().getClients().size(), 2);
        assertNotNull(nzyme.getClients().getClients().get("3c:8d:20:25:20:e9"));
        assertNotNull(nzyme.getClients().getClients().get("ac:81:12:d2:26:7e"));

        // assert getNetworks() getClients()
    }

}