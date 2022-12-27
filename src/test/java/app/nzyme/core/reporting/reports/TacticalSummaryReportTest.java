package app.nzyme.core.reporting.reports;

import app.nzyme.core.MockNzyme;
import app.nzyme.core.NzymeLeader;
import app.nzyme.core.alerts.PwnagotchiAdvertisementAlert;
import app.nzyme.core.alerts.UnknownSSIDAlert;
import app.nzyme.core.dot11.Dot11MetaInformation;
import app.nzyme.core.dot11.MalformedFrameException;
import app.nzyme.core.dot11.interceptors.misc.PwnagotchiAdvertisement;
import app.nzyme.core.events.BrokenProbeEvent;
import app.nzyme.core.events.StartupEvent;
import org.joda.time.DateTime;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

public class TacticalSummaryReportTest {

    protected static final Dot11MetaInformation META_NO_WEP = new Dot11MetaInformation(false, 100, 2400, 1, 0L, false);

    @BeforeMethod
    public void cleanDatabase() {
        NzymeLeader nzyme = new MockNzyme();
        nzyme.getDatabase().useHandle(handle -> handle.execute("DELETE FROM alerts"));
        nzyme.getDatabase().useHandle(handle -> handle.execute("DELETE FROM events"));
    }

    @Test
    public void testBasicReport() throws Exception, MalformedFrameException {
        MockNzyme nzyme = new MockNzyme(5);

        nzyme.getSentry().tickSSID("Centurion_Lounge", DateTime.now().minusDays(162));
        nzyme.getSentry().tickSSID("Centurion_Lounge", DateTime.now().minusSeconds(4));
        nzyme.getSentry().tickSSID("United_WiFi", DateTime.now().minusDays(172));
        nzyme.getSentry().tickSSID("United_WiFi", DateTime.now());
        nzyme.getSentry().tickSSID("MobileHotspot5233", DateTime.now().minusHours(23));
        nzyme.getSentry().tickSSID("MobileHotspot5233", DateTime.now().minusHours(22).minusMinutes(45));
        nzyme.getSentry().tickSSID("MySweetHome", DateTime.now().minusDays(163));
        nzyme.getSentry().tickSSID("MySweetHome", DateTime.now());

        nzyme.getSentry().stop();

        nzyme.getAlertsService().handle(UnknownSSIDAlert.create(new DateTime(), "Centurion_Lounge", "8F:F0:17:E8:68:28", 11, 1234, 0));
        nzyme.getAlertsService().handle(UnknownSSIDAlert.create(new DateTime(), "United_WiFi", "9C:29:9E:C7:74:52", 11, 1234, 0));
        nzyme.getAlertsService().handle(PwnagotchiAdvertisementAlert.create(new DateTime(), PwnagotchiAdvertisement.create("james", "1.1", "123", 0D, 0, 0), 11, 1234, 0, 1));

        nzyme.getEventService().recordEvent(new StartupEvent());
        nzyme.getEventService().recordEvent(new BrokenProbeEvent("foo-probe-1", "foo,bar"));

        TacticalSummaryReport.Report report = new TacticalSummaryReport.Report();
        report.runReport(nzyme, null);
    }

}