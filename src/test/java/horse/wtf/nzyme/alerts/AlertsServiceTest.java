package horse.wtf.nzyme.alerts;

import com.google.common.base.Strings;
import com.google.common.collect.Lists;
import horse.wtf.nzyme.MockNzyme;
import horse.wtf.nzyme.Nzyme;
import horse.wtf.nzyme.dot11.probes.Dot11MockProbe;
import horse.wtf.nzyme.dot11.probes.Dot11Probe;
import horse.wtf.nzyme.notifications.uplinks.misc.LoopbackUplink;
import horse.wtf.nzyme.statistics.Statistics;
import org.testng.annotations.Test;

import java.util.UUID;
import java.util.concurrent.TimeUnit;

import static org.testng.Assert.*;

public class AlertsServiceTest extends AlertTestHelper {

    // TODO: updating last_seen works for sameAs alerts

    @Test
    public void testRetentionCleaning() {
        AlertsService as = new AlertsService(
                new MockNzyme(),
                100,
                TimeUnit.MILLISECONDS,
                1,
                TimeUnit.SECONDS
        );

        as.handle(UnexpectedSSIDBeaconAlert.create(
                "wtf",
                "00:c0:ca:95:68:3b",
                META_NO_WEP,
                buildMockProbe(BANDITS_STANDARD)
        ));

        assertEquals(as.getActiveAlerts().size(), 1);

        try {
            Thread.sleep(250);
        } catch (InterruptedException e) {}

        as.handle(UnexpectedChannelBeaconAlert.create(
                "wtf",
                "00:c0:ca:95:68:3b",
                META_NO_WEP,
                buildMockProbe(BANDITS_STANDARD)
        ));

        assertEquals(as.getActiveAlerts().size(), 2);

        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {}

        assertEquals(as.getActiveAlerts().size(), 1);

        try {
            Thread.sleep(500);
        } catch (InterruptedException e) {}

        assertEquals(as.getActiveAlerts().size(), 0);
    }

    @Test(expectedExceptions = UnsupportedOperationException.class)
    public void testGetActiveAlertsReturnsImmutableCopyPut() {
        new AlertsService(new MockNzyme()).getActiveAlerts().put(UUID.randomUUID(), UnexpectedSSIDBeaconAlert.create(
                "wtf",
                "00:c0:ca:95:68:3b",
                META_NO_WEP,
                buildMockProbe(BANDITS_STANDARD)
        ));
    }

    @Test(expectedExceptions = UnsupportedOperationException.class)
    public void testGetActiveAlertsReturnsImmutableCopyRemove() {
        new AlertsService(new MockNzyme()).getActiveAlerts().remove(UUID.randomUUID());
    }

    @Test
    public void testSetsUUID() {
        AlertsService as = new AlertsService(new MockNzyme());

        as.handle(UnexpectedSSIDBeaconAlert.create(
                "wtf",
                "00:c0:ca:95:68:3b",
                META_NO_WEP,
                buildMockProbe(BANDITS_STANDARD)
        ));

        Alert alert = Lists.newArrayList(as.getActiveAlerts().values()).get(0);
        assertNotNull(alert);
        assertFalse(Strings.isNullOrEmpty(alert.getUUID().toString()));
    }

    @Test
    public void testUplinkConnection() {
        Nzyme nzyme = new MockNzyme();
        Dot11Probe probe = new Dot11MockProbe(nzyme, CONFIG_STANDARD, new Statistics());
        LoopbackUplink loopback = new LoopbackUplink();
        probe.registerUplink(loopback);

        AlertsService as = new AlertsService(nzyme);
        as.handle(UnexpectedSSIDBeaconAlert.create(
                "wtf",
                "00:c0:ca:95:68:3b",
                META_NO_WEP,
                probe
        ));

        assertNotNull(loopback.getLastAlert());
        assertEquals(loopback.getLastAlert().getMessage(), "Our BSSID [00:c0:ca:95:68:3b] advertised unexpected SSID [wtf] with beacon frame.");
        assertEquals(loopback.getLastAlert().getClass(), UnexpectedSSIDBeaconAlert.class);
    }

}