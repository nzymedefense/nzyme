package app.nzyme.core.dot11.interceptors;

import com.codahale.metrics.MetricRegistry;
import app.nzyme.core.MockNzyme;
import app.nzyme.core.NzymeLeader;
import app.nzyme.core.alerts.UnknownSSIDAlert;
import app.nzyme.core.dot11.Dot11FrameInterceptor;
import app.nzyme.core.dot11.MalformedFrameException;
import app.nzyme.core.dot11.anonymization.Anonymizer;
import app.nzyme.core.dot11.networks.sentry.Sentry;
import app.nzyme.core.dot11.parsers.Dot11BeaconFrameParser;
import app.nzyme.core.dot11.parsers.Dot11ProbeResponseFrameParser;
import app.nzyme.core.dot11.parsers.Frames;
import app.nzyme.core.notifications.uplinks.misc.LoopbackUplink;
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