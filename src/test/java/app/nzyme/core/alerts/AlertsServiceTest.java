package app.nzyme.core.alerts;

import app.nzyme.core.NzymeNode;
import com.google.common.base.Strings;
import com.google.common.collect.Lists;
import app.nzyme.core.MockNzyme;
import app.nzyme.core.alerts.service.AlertsService;
import app.nzyme.core.notifications.uplinks.misc.LoopbackUplink;
import org.joda.time.DateTime;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.util.ArrayList;
import java.util.UUID;

import static org.testng.Assert.*;

public class AlertsServiceTest extends AlertTestHelper {

    // TODO: updating last_seen works for sameAs alerts
    // TODO: each alert type can be serialized from db

    @BeforeMethod
    public void cleanAlerts() {
        NzymeNode nzyme = new MockNzyme();
        nzyme.getDatabase().useHandle(handle -> handle.execute(CLEAR_QUERY));
    }

    @Test
    public void testSameAlertsAreNotDuplicatedAndLastSeenIsUpdated() {
        NzymeNode nzyme = new MockNzyme();

        AlertsService as = new AlertsService(nzyme);

        as.handle(UnexpectedSSIDBeaconAlert.create(
                DateTime.now(),
                "wtf",
                "00:c0:ca:95:68:3b",
                1,
                1000,
                -50,
                1
        ));

        assertEquals(as.findActiveAlerts().size(), 1);
        Alert a1 = new ArrayList<>(as.findActiveAlerts().values()).get(0);
        DateTime lastSeen = a1.getLastSeen();
        assertNotNull(lastSeen);
        
        try {
            Thread.sleep(500);
        } catch (InterruptedException e) {}

        // Same alert as above.
        as.handle(UnexpectedSSIDBeaconAlert.create(
                DateTime.now(),
                "wtf",
                "00:c0:ca:95:68:3b",
                1,
                1000,
                -50,
                1
        ));

        try {
            Thread.sleep(1500);
        } catch (InterruptedException e) {}

        assertEquals(as.findActiveAlerts().size(), 1);
        Alert a1a = new ArrayList<>(as.findActiveAlerts().values()).get(0);

        assertEquals(a1.getUUID(), a1a.getUUID());
        assertEquals(a1.getFirstSeen(), a1a.getFirstSeen());
        assertNotEquals(lastSeen, a1a.getLastSeen());
        assertEquals(a1a.getFrameCount(), (Long) 2L);
    }

    @Test(expectedExceptions = UnsupportedOperationException.class)
    public void testGetActiveAlertsReturnsImmutableCopyPut() {
        NzymeNode nzyme = new MockNzyme();

        new AlertsService(nzyme).findActiveAlerts().put(UUID.randomUUID(), UnexpectedSSIDBeaconAlert.create(
                DateTime.now(),
                "wtf",
                "00:c0:ca:95:68:3b",
                1,
                1000,
                -50,
                1
        ));
    }

    @Test(expectedExceptions = UnsupportedOperationException.class)
    public void testGetActiveAlertsReturnsImmutableCopyRemove() {
        new AlertsService(new MockNzyme()).findActiveAlerts().remove(UUID.randomUUID());
    }

    @Test
    public void testSetsUUID() {
        NzymeNode nzyme = new MockNzyme();

        AlertsService as = new AlertsService(nzyme);

        as.handle(UnexpectedSSIDBeaconAlert.create(
                DateTime.now(),
                "wtf",
                "00:c0:ca:95:68:3b",
                1,
                1000,
                -50,
                1
        ));

        Alert alert = Lists.newArrayList(as.findActiveAlerts().values()).get(0);
        assertNotNull(alert);
        assertFalse(Strings.isNullOrEmpty(alert.getUUID().toString()));
    }

    @Test
    public void testUplinkConnection() {
        NzymeNode nzyme = new MockNzyme();

        LoopbackUplink loopback = new LoopbackUplink();
        nzyme.registerUplink(loopback);

        AlertsService as = new AlertsService(nzyme);
        as.handle(UnexpectedSSIDBeaconAlert.create(
                DateTime.now(),
                "wtf",
                "00:c0:ca:95:68:3b",
                1,
                1000,
                -50,
                1
        ));

        assertNotNull(loopback.getLastAlert());
        assertEquals(loopback.getLastAlert().getMessage(), "Our BSSID [00:c0:ca:95:68:3b] advertised unexpected SSID [wtf] with beacon frame.");
        assertEquals(loopback.getLastAlert().getClass(), UnexpectedSSIDBeaconAlert.class);
    }

    @Test
    public void testDoesNotFailOnMissingMilliseconds() {
        NzymeNode nzyme = new MockNzyme();

        LoopbackUplink loopback = new LoopbackUplink();
        nzyme.registerUplink(loopback);

        AlertsService as = new AlertsService(nzyme);
        as.handle(UnexpectedSSIDBeaconAlert.create(
                DateTime.now().withMillisOfSecond(0),
                "wtf",
                "00:c0:ca:95:68:3b",
                1,
                1000,
                -50,
                1
        ));

        // Set timestamps to a value without milliseconds.
        nzyme.getDatabase().withHandle(handle ->
            handle.execute("UPDATE alerts SET first_seen = '2021-06-14 12:33:25', last_seen = '2021-06-14 12:33:25'")
        );

        as.findAllAlerts(100, 0);
    }

}