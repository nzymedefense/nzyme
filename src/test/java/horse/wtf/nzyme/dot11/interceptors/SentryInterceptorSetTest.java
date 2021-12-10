package horse.wtf.nzyme.dot11.interceptors;

import com.codahale.metrics.MetricRegistry;
import horse.wtf.nzyme.MockNzyme;
import horse.wtf.nzyme.NzymeLeader;
import horse.wtf.nzyme.alerts.UnknownSSIDAlert;
import horse.wtf.nzyme.dot11.Dot11FrameInterceptor;
import horse.wtf.nzyme.dot11.MalformedFrameException;
import horse.wtf.nzyme.dot11.anonymization.Anonymizer;
import horse.wtf.nzyme.dot11.networks.sentry.Sentry;
import horse.wtf.nzyme.dot11.parsers.Dot11BeaconFrameParser;
import horse.wtf.nzyme.dot11.parsers.Dot11ProbeResponseFrameParser;
import horse.wtf.nzyme.dot11.parsers.Frames;
import horse.wtf.nzyme.notifications.uplinks.misc.LoopbackUplink;
import org.pcap4j.packet.IllegalRawDataException;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import static org.testng.Assert.*;

public class SentryInterceptorSetTest extends InterceptorSetTest {

    @BeforeMethod
    public void cleanSentry() {
        NzymeLeader nzyme = new MockNzyme();
        nzyme.getDatabase().useHandle(handle -> handle.execute("DELETE FROM sentry_ssids;"));
        nzyme.getDatabase().useHandle(handle -> handle.execute("DELETE FROM alerts;"));
    }

    @Test
    public void testBeaconWithAlertDisabled() throws MalformedFrameException, IllegalRawDataException, InterruptedException {
        LoopbackUplink uplink = new LoopbackUplink();
        NzymeLeader nzyme = new MockNzyme();
        nzyme.registerUplink(uplink);
        Sentry sentry = new Sentry(nzyme, 2);

        try {
            assertEquals(sentry.getSSIDs().size(), 0);
            assertNull(uplink.getLastAlert());

            Dot11FrameInterceptor interceptor = new SentryInterceptorSet(sentry, nzyme.getAlertsService(), false).getInterceptors().get(0);

            interceptor.intercept(new Dot11BeaconFrameParser(new MetricRegistry(), new Anonymizer(false, "")).parse(
                    Frames.BEACON_1_PAYLOAD, Frames.BEACON_1_HEADER, META_NO_WEP
            ));

            Thread.sleep(2500);

            assertEquals(sentry.getSSIDs().size(), 1);
            assertTrue(sentry.knowsSSID("WTF"));
            assertNull(uplink.getLastAlert());
        } finally {
            sentry.stop();
        }
    }

    @Test
    public void testBeaconWithAlertEnabled() throws MalformedFrameException, IllegalRawDataException, InterruptedException {
        LoopbackUplink uplink = new LoopbackUplink();
        NzymeLeader nzyme = new MockNzyme();
        nzyme.registerUplink(uplink);
        Sentry sentry = new Sentry(nzyme, 2);

        try {
            assertEquals(sentry.getSSIDs().size(), 0);
            assertNull(uplink.getLastAlert());

            Dot11FrameInterceptor interceptor = new SentryInterceptorSet(sentry, nzyme.getAlertsService(), true).getInterceptors().get(0);

            interceptor.intercept(new Dot11BeaconFrameParser(new MetricRegistry(), new Anonymizer(false, "")).parse(
                    Frames.BEACON_1_PAYLOAD, Frames.BEACON_1_HEADER, META_NO_WEP
            ));

            Thread.sleep(2500);

            assertEquals(sentry.getSSIDs().size(), 1);
            assertTrue(sentry.knowsSSID("WTF"));
            assertNotNull(uplink.getLastAlert());
            assertEquals(uplink.getLastAlert().getClass(), UnknownSSIDAlert.class);
        } finally {
            sentry.stop();
        }
    }

    @Test
    public void testProbeRespWithAlertDisabled() throws MalformedFrameException, IllegalRawDataException, InterruptedException {

        LoopbackUplink uplink = new LoopbackUplink();
        NzymeLeader nzyme = new MockNzyme();
        nzyme.registerUplink(uplink);
        Sentry sentry = new Sentry(nzyme, 2);

        try {
            assertEquals(sentry.getSSIDs().size(), 0);
            assertNull(uplink.getLastAlert());

            Dot11FrameInterceptor interceptor = new SentryInterceptorSet(sentry, nzyme.getAlertsService(), false).getInterceptors().get(1);

            interceptor.intercept(new Dot11ProbeResponseFrameParser(new MetricRegistry(), new Anonymizer(false, "")).parse(
                    Frames.PROBE_RESP_1_PAYLOAD, Frames.PROBE_RESP_1_HEADER, META_NO_WEP
            ));

            Thread.sleep(2500);

            assertEquals(sentry.getSSIDs().size(), 1);
            assertTrue(sentry.knowsSSID("Home 5F48"));
            assertNull(uplink.getLastAlert());
        } finally {
            sentry.stop();
        }
    }

    @Test
    public void testProbeRespWithAlertEnabled() throws MalformedFrameException, IllegalRawDataException, InterruptedException {

        LoopbackUplink uplink = new LoopbackUplink();
        NzymeLeader nzyme = new MockNzyme();
        nzyme.registerUplink(uplink);
        Sentry sentry = new Sentry(nzyme, 2);

        try {
            assertEquals(sentry.getSSIDs().size(), 0);
            assertNull(uplink.getLastAlert());

            Dot11FrameInterceptor interceptor = new SentryInterceptorSet(sentry, nzyme.getAlertsService(), true).getInterceptors().get(1);

            interceptor.intercept(new Dot11ProbeResponseFrameParser(new MetricRegistry(), new Anonymizer(false, "")).parse(
                    Frames.PROBE_RESP_1_PAYLOAD, Frames.PROBE_RESP_1_HEADER, META_NO_WEP
            ));

            Thread.sleep(2500);

            assertEquals(sentry.getSSIDs().size(), 1);
            assertTrue(sentry.knowsSSID("Home 5F48"));
            assertNotNull(uplink.getLastAlert());
            assertEquals(uplink.getLastAlert().getClass(), UnknownSSIDAlert.class);
        } finally {
            sentry.stop();
        }
    }

}