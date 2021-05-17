package horse.wtf.nzyme.scheduler.reporting;

import horse.wtf.nzyme.MockNzyme;
import org.testng.annotations.Test;

import java.io.FileWriter;

import static org.testng.Assert.*;

public class TacticalSummaryReportTest {

    @Test
    public void testBasicReport() throws Exception {
        MockNzyme nzyme = new MockNzyme();

        TacticalSummaryReport.Report report = new TacticalSummaryReport.Report();
        report.runReport(nzyme, new FileWriter("/tmp/report.html"));
    }

}