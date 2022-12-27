package app.nzyme.core.monitoring.prometheus;

import app.nzyme.core.MockNzyme;
import app.nzyme.core.NzymeLeader;
import org.testng.annotations.Test;

public class PrometheusFormatterTest {

    @Test
    public void testFormat() {
        NzymeLeader nzyme = new MockNzyme();

        // Create some metrics data.
        nzyme.getMetrics().timer("app.nzyme.test.some-timer_from--somewhere").time().stop();
        nzyme.getMetrics().histogram("app.nzyme.test.some-histo_from--somewhere").update(5);
        nzyme.getMetrics().histogram("app.nzyme.test.some-histo_from--somewhere").update(12);
        nzyme.getMetrics().counter("app.nzyme.test.some-counter_from--somewhere").inc(100);
        nzyme.getMetrics().meter("app.nzyme.test.some-meter_from--somewhere").mark();

        PrometheusFormatter f = new PrometheusFormatter(nzyme.getMetrics());
        f.format(); // Currently only running to make sure there are no exceptions. Needs some better tests.
    }

}