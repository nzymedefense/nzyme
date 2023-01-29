package app.nzyme.core.dot11.interceptors;

import app.nzyme.core.NzymeNode;
import com.codahale.metrics.MetricRegistry;
import app.nzyme.core.MockNzyme;
import app.nzyme.core.bandits.Bandit;
import app.nzyme.core.bandits.identifiers.BanditIdentifier;
import app.nzyme.core.bandits.identifiers.SSIDIBanditdentifier;
import app.nzyme.core.bandits.identifiers.SignalStrengthBanditIdentifier;
import app.nzyme.core.dot11.Dot11FrameInterceptor;
import app.nzyme.core.dot11.Dot11FrameSubtype;
import app.nzyme.core.dot11.Dot11MetaInformation;
import app.nzyme.core.dot11.MalformedFrameException;
import app.nzyme.core.dot11.anonymization.Anonymizer;
import app.nzyme.core.dot11.parsers.Dot11BeaconFrameParser;
import app.nzyme.core.dot11.parsers.Dot11DeauthenticationFrameParser;
import app.nzyme.core.dot11.parsers.Dot11ProbeResponseFrameParser;
import app.nzyme.core.dot11.parsers.Frames;
import app.nzyme.core.notifications.uplinks.misc.LoopbackUplink;
import org.joda.time.DateTime;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.util.ArrayList;
import java.util.UUID;
import java.util.function.Supplier;

import static org.testng.Assert.*;

public class BanditIdentifierInterceptorSetTest extends InterceptorSetTest {

    @BeforeMethod
    public void cleanDatabase() {
        NzymeNode nzyme = new MockNzyme();
        nzyme.getDatabase().useHandle(handle -> handle.execute("DELETE FROM bandits"));
    }

    @Test
    public void testGetInterceptors() throws MalformedFrameException, Exception {
        NzymeNode nzyme = new MockNzyme();
        LoopbackUplink loopback = new LoopbackUplink();
        nzyme.registerUplink(loopback);

        UUID bandit1UUID = UUID.randomUUID();
        nzyme.getContactManager().registerBandit(Bandit.create(
                null, bandit1UUID, "foo", "foo", false, DateTime.now(), DateTime.now(),
                new ArrayList<BanditIdentifier>() {{
                    add(new SSIDIBanditdentifier(new ArrayList<String>(){{ add("WTF"); }}, null, UUID.randomUUID()));
                }}
        ));

        UUID bandit2UUID = UUID.randomUUID();
        nzyme.getContactManager().registerBandit(Bandit.create(
                null, bandit2UUID, "foo", "foo", false, DateTime.now(), DateTime.now(),
                new ArrayList<BanditIdentifier>() {{
                    add(new SignalStrengthBanditIdentifier(-80, -90, null, UUID.randomUUID()));
                }}
        ));

        Bandit bandit1 = nzyme.getContactManager().findBanditByUUID(bandit1UUID).orElseThrow((Supplier<Exception>) RuntimeException::new);
        Bandit bandit2 = nzyme.getContactManager().findBanditByUUID(bandit2UUID).orElseThrow((Supplier<Exception>) RuntimeException::new);
        assertFalse(nzyme.getContactManager().banditHasActiveContactOnSource(bandit1, nzyme.getNodeInformation().name()));
        assertFalse(nzyme.getContactManager().banditHasActiveContactOnSource(bandit2, nzyme.getNodeInformation().name()));

        BanditIdentifierInterceptorSet set = new BanditIdentifierInterceptorSet(nzyme.getContactManager());
        assertEquals(set.getInterceptors().size(), 3);

        for (Dot11FrameInterceptor interceptor : set.getInterceptors()) {
            if (interceptor.forSubtype() == Dot11FrameSubtype.BEACON) {
                assertTrue(interceptor.raisesAlerts().isEmpty());

                // Beacon for a different SSID.
                interceptor.intercept(new Dot11BeaconFrameParser(new MetricRegistry(), new Anonymizer(false, "")).parse(
                        Frames.BEACON_3_PAYLOAD, Frames.BEACON_3_HEADER, META_NO_WEP
                ));
                assertFalse(nzyme.getContactManager().banditHasActiveContactOnSource(bandit1, nzyme.getNodeInformation().name()));

                // Beacon for bandit SSID.
                interceptor.intercept(new Dot11BeaconFrameParser(new MetricRegistry(), new Anonymizer(false, "")).parse(
                        Frames.BEACON_1_PAYLOAD, Frames.BEACON_1_HEADER, META_NO_WEP
                ));
                assertTrue(nzyme.getContactManager().banditHasActiveContactOnSource(bandit1, nzyme.getNodeInformation().name()));
            }

            if (interceptor.forSubtype() == Dot11FrameSubtype.PROBE_RESPONSE) {
                assertTrue(interceptor.raisesAlerts().isEmpty());

                // Probe-resp for a different SSID.
                interceptor.intercept(new Dot11ProbeResponseFrameParser(new MetricRegistry(), new Anonymizer(false, "")).parse(
                        Frames.PROBE_RESP_1_PAYLOAD, Frames.PROBE_RESP_1_HEADER, META_NO_WEP
                ));
                assertFalse(nzyme.getContactManager().banditHasActiveContactOnSource(bandit1, nzyme.getNodeInformation().name()));

                // Probe-resp for bandit SSID.
                interceptor.intercept(new Dot11ProbeResponseFrameParser(new MetricRegistry(), new Anonymizer(false, "")).parse(
                        Frames.PROBE_RESP_2_PAYLOAD, Frames.PROBE_RESP_2_HEADER, META_NO_WEP
                ));
                assertTrue(nzyme.getContactManager().banditHasActiveContactOnSource(bandit1, nzyme.getNodeInformation().name()));
            }

            if (interceptor.forSubtype() == Dot11FrameSubtype.DEAUTHENTICATION) {
                assertTrue(interceptor.raisesAlerts().isEmpty());

                // Probe-resp for a different SSID.
                interceptor.intercept(new Dot11DeauthenticationFrameParser(new MetricRegistry(), new Anonymizer(false, "")).parse(
                        Frames.DEAUTH_1_PAYLOAD, Frames.DEAUTH_1_HEADER, new Dot11MetaInformation(false, -50, 1000, 9001, 0L, false)
                ));
                assertFalse(nzyme.getContactManager().banditHasActiveContactOnSource(bandit2, nzyme.getNodeInformation().name()));

                // Probe-resp for bandit SSID.
                interceptor.intercept(new Dot11DeauthenticationFrameParser(new MetricRegistry(), new Anonymizer(false, "")).parse(
                        Frames.DEAUTH_1_PAYLOAD, Frames.DEAUTH_1_HEADER, new Dot11MetaInformation(false, -85, 1000, 9001, 0L, false)
                ));
                assertTrue(nzyme.getContactManager().banditHasActiveContactOnSource(bandit2, nzyme.getNodeInformation().name()));
            }

            loopback.clear();
            nzyme.getDatabase().useHandle(handle -> handle.execute("DELETE FROM contacts"));
        }
    }

}