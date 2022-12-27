package app.nzyme.core.dot11.interceptors;

import com.codahale.metrics.MetricRegistry;
import app.nzyme.core.MockNzyme;
import app.nzyme.core.NzymeLeader;
import app.nzyme.core.alerts.Alert;
import app.nzyme.core.alerts.PwnagotchiAdvertisementAlert;
import app.nzyme.core.dot11.MalformedFrameException;
import app.nzyme.core.dot11.anonymization.Anonymizer;
import app.nzyme.core.dot11.parsers.Dot11BeaconFrameParser;
import app.nzyme.core.dot11.parsers.Frames;
import app.nzyme.core.notifications.uplinks.misc.LoopbackUplink;
import org.pcap4j.packet.IllegalRawDataException;
import org.testng.annotations.Test;

import java.util.ArrayList;

import static org.testng.Assert.*;

public class PwnagotchiAdvertisementInterceptorTest extends InterceptorSetTest {

    @Test
    public void testInterceptor() throws MalformedFrameException, IllegalRawDataException {
        NzymeLeader nzyme = new MockNzyme();
        LoopbackUplink loopback = new LoopbackUplink();
        nzyme.registerUplink(loopback);
        PwnagotchiAdvertisementInterceptor interceptor = new PwnagotchiAdvertisementInterceptor(nzyme.getAlertsService());

        assertEquals(interceptor.raisesAlerts(), new ArrayList<Class<? extends Alert>>(){{ add(PwnagotchiAdvertisementAlert.class); }});
        interceptor.intercept(new Dot11BeaconFrameParser(new MetricRegistry(), new Anonymizer(false, "")).parse(
                Frames.BEACON_1_PAYLOAD, Frames.BEACON_1_HEADER, META_NO_WEP
        ));
        assertNull(loopback.getLastAlert());

        interceptor.intercept(new Dot11BeaconFrameParser(new MetricRegistry(), new Anonymizer(false, "")).parse(
                Frames.PWNAGOTCHI_ADVERTISEMENT_BEACON_1_PAYLOAD, Frames.PWNAGOTCHI_ADVERTISEMENT_BEACON_1_HEADER, META_NO_WEP
        ));
        assertNotNull(loopback.getLastAlert());
        assertEquals(PwnagotchiAdvertisementAlert.class, loopback.getLastAlert().getClass());
    }

}