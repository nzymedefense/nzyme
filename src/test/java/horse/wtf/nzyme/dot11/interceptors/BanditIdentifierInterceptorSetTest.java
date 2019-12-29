package horse.wtf.nzyme.dot11.interceptors;

import com.codahale.metrics.MetricRegistry;
import horse.wtf.nzyme.MockNzyme;
import horse.wtf.nzyme.Nzyme;
import horse.wtf.nzyme.bandits.Bandit;
import horse.wtf.nzyme.bandits.identifiers.BanditIdentifier;
import horse.wtf.nzyme.bandits.identifiers.SSIDIBanditdentifier;
import horse.wtf.nzyme.bandits.identifiers.SignalStrengthBanditIdentifier;
import horse.wtf.nzyme.dot11.Dot11FrameInterceptor;
import horse.wtf.nzyme.dot11.Dot11FrameSubtype;
import horse.wtf.nzyme.dot11.Dot11MetaInformation;
import horse.wtf.nzyme.dot11.MalformedFrameException;
import horse.wtf.nzyme.dot11.parsers.Dot11BeaconFrameParser;
import horse.wtf.nzyme.dot11.parsers.Dot11DeauthenticationFrameParser;
import horse.wtf.nzyme.dot11.parsers.Dot11ProbeResponseFrameParser;
import horse.wtf.nzyme.dot11.parsers.Frames;
import horse.wtf.nzyme.notifications.uplinks.misc.LoopbackUplink;
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
        Nzyme nzyme = new MockNzyme();
        nzyme.getDatabase().useHandle(handle -> handle.execute("DELETE FROM bandits"));
    }

    @Test
    public void testGetInterceptors() throws MalformedFrameException, Exception {
        Nzyme nzyme = new MockNzyme();
        LoopbackUplink loopback = new LoopbackUplink();
        nzyme.registerUplink(loopback);

        UUID bandit1UUID = UUID.randomUUID();
        nzyme.getContactIdentifier().registerBandit(Bandit.create(
                null, bandit1UUID, "foo", "foo", DateTime.now(), DateTime.now(),
                new ArrayList<BanditIdentifier>() {{
                    add(new SSIDIBanditdentifier(new ArrayList<String>(){{ add("WTF"); }}));
                }}
        ));

        UUID bandit2UUID = UUID.randomUUID();
        nzyme.getContactIdentifier().registerBandit(Bandit.create(
                null, bandit2UUID, "foo", "foo", DateTime.now(), DateTime.now(),
                new ArrayList<BanditIdentifier>() {{
                    add(new SignalStrengthBanditIdentifier(-80, -90));
                }}
        ));

        Bandit bandit1 = nzyme.getContactIdentifier().findBanditByUUID(bandit1UUID).orElseThrow((Supplier<Exception>) RuntimeException::new);
        Bandit bandit2 = nzyme.getContactIdentifier().findBanditByUUID(bandit2UUID).orElseThrow((Supplier<Exception>) RuntimeException::new);
        assertFalse(nzyme.getContactIdentifier().banditHasActiveContact(bandit1));
        assertFalse(nzyme.getContactIdentifier().banditHasActiveContact(bandit2));

        BanditIdentifierInterceptorSet set = new BanditIdentifierInterceptorSet(nzyme);
        assertEquals(set.getInterceptors().size(), 3);

        for (Dot11FrameInterceptor interceptor : set.getInterceptors()) {
            if (interceptor.forSubtype() == Dot11FrameSubtype.BEACON) {
                assertTrue(interceptor.raisesAlerts().isEmpty());

                // Beacon for a different SSID.
                interceptor.intercept(new Dot11BeaconFrameParser(new MetricRegistry()).parse(
                        Frames.BEACON_3_PAYLOAD, Frames.BEACON_3_HEADER, META_NO_WEP
                ));
                assertFalse(nzyme.getContactIdentifier().banditHasActiveContact(bandit1));

                // Beacon for bandit SSID.
                interceptor.intercept(new Dot11BeaconFrameParser(new MetricRegistry()).parse(
                        Frames.BEACON_1_PAYLOAD, Frames.BEACON_1_HEADER, META_NO_WEP
                ));
                assertTrue(nzyme.getContactIdentifier().banditHasActiveContact(bandit1));
            }

            if (interceptor.forSubtype() == Dot11FrameSubtype.PROBE_RESPONSE) {
                assertTrue(interceptor.raisesAlerts().isEmpty());

                // Probe-resp for a different SSID.
                interceptor.intercept(new Dot11ProbeResponseFrameParser(new MetricRegistry()).parse(
                        Frames.PROBE_RESP_1_PAYLOAD, Frames.PROBE_RESP_1_HEADER, META_NO_WEP
                ));
                assertFalse(nzyme.getContactIdentifier().banditHasActiveContact(bandit1));

                // Probe-resp for bandit SSID.
                interceptor.intercept(new Dot11ProbeResponseFrameParser(new MetricRegistry()).parse(
                        Frames.PROBE_RESP_2_PAYLOAD, Frames.PROBE_RESP_2_HEADER, META_NO_WEP
                ));
                assertTrue(nzyme.getContactIdentifier().banditHasActiveContact(bandit1));
            }

            if (interceptor.forSubtype() == Dot11FrameSubtype.DEAUTHENTICATION) {
                assertTrue(interceptor.raisesAlerts().isEmpty());

                // Probe-resp for a different SSID.
                interceptor.intercept(new Dot11DeauthenticationFrameParser(new MetricRegistry()).parse(
                        Frames.DEAUTH_1_PAYLOAD, Frames.DEAUTH_1_HEADER, new Dot11MetaInformation(false, -50, 1000, 9001, 0L, false)
                ));
                assertFalse(nzyme.getContactIdentifier().banditHasActiveContact(bandit2));

                // Probe-resp for bandit SSID.
                interceptor.intercept(new Dot11DeauthenticationFrameParser(new MetricRegistry()).parse(
                        Frames.DEAUTH_1_PAYLOAD, Frames.DEAUTH_1_HEADER, new Dot11MetaInformation(false, -85, 1000, 9001, 0L, false)
                ));
                assertTrue(nzyme.getContactIdentifier().banditHasActiveContact(bandit2));
            }

            loopback.clear();
            nzyme.getDatabase().useHandle(handle -> handle.execute("DELETE FROM contacts"));
        }
    }

}