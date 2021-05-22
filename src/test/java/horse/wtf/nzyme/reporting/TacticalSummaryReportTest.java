package horse.wtf.nzyme.reporting;

import horse.wtf.nzyme.MockNzyme;
import horse.wtf.nzyme.NzymeLeader;
import horse.wtf.nzyme.alerts.PwnagotchiAdvertisementAlert;
import horse.wtf.nzyme.alerts.UnknownSSIDAlert;
import horse.wtf.nzyme.dot11.interceptors.misc.PwnagotchiAdvertisement;
import horse.wtf.nzyme.reporting.reports.TacticalSummaryReport;
import org.joda.time.DateTime;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.io.FileWriter;

public class TacticalSummaryReportTest {

    @BeforeMethod
    public void cleanAlerts() {
        NzymeLeader nzyme = new MockNzyme();
        nzyme.getDatabase().useHandle(handle -> handle.execute("DELETE FROM alerts"));
    }

    @Test
    public void testBasicReport() throws Exception {
        MockNzyme nzyme = new MockNzyme();

        nzyme.getAlertsService().handle(UnknownSSIDAlert.create(new DateTime(), "Centurion_Lounge", "8F:F0:17:E8:68:28", 11, 1234, 0));
        nzyme.getAlertsService().handle(UnknownSSIDAlert.create(new DateTime(), "United_WiFi", "9C:29:9E:C7:74:52", 11, 1234, 0));
        nzyme.getAlertsService().handle(PwnagotchiAdvertisementAlert.create(new DateTime(), PwnagotchiAdvertisement.create("james", "1.1", "123", 0D, 0, 0), 11, 1234, 0, 1));

        TacticalSummaryReport.Report report = new TacticalSummaryReport.Report();
        report.runReport(nzyme, new FileWriter("/tmp/report.html"));
    }

}