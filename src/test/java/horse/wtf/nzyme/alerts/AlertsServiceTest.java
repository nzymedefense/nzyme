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

import static org.testng.Assert.*;

public class AlertsServiceTest extends AlertTestHelper {

    // TODO: fucking with a copy doesn't fuck with original, is immutable
    // TODO: retention cleaning old alerts works
    // TODO: updating last_seen works for sameAs alerts

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