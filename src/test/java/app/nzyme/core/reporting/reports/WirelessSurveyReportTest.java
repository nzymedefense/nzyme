package app.nzyme.core.reporting.reports;

import app.nzyme.core.MockNzyme;
import app.nzyme.core.dot11.Dot11MetaInformation;
import app.nzyme.core.dot11.MalformedFrameException;
import org.joda.time.DateTime;
import org.testng.annotations.Test;

import java.util.concurrent.TimeUnit;

public class WirelessSurveyReportTest {

    protected static final Dot11MetaInformation META_NO_WEP = new Dot11MetaInformation(false, 100, 2400, 1, 0L, false);

    @Test
    public void testBasicReport() throws Exception, MalformedFrameException {
        MockNzyme nzyme = new MockNzyme(5, 5, TimeUnit.SECONDS);

        nzyme.getSentry().tickSSID("Centurion_Lounge", DateTime.now().minusDays(162));
        nzyme.getSentry().tickSSID("Centurion_Lounge", DateTime.now().minusSeconds(4));
        nzyme.getSentry().tickSSID("United_WiFi", DateTime.now().minusDays(172));
        nzyme.getSentry().tickSSID("United_WiFi", DateTime.now());
        nzyme.getSentry().tickSSID("MobileHotspot5233", DateTime.now().minusHours(23));
        nzyme.getSentry().tickSSID("MobileHotspot5233", DateTime.now().minusHours(22).minusMinutes(45));
        nzyme.getSentry().tickSSID("MySweetHome", DateTime.now().minusDays(163));
        nzyme.getSentry().tickSSID("MySweetHome", DateTime.now());

        nzyme.getSentry().stop();

        WirelessSurveyReport.Report report = new WirelessSurveyReport.Report();
        report.runReport(nzyme, null);
    }

}