package horse.wtf.nzyme.monitoring.prometheus;

import com.codahale.metrics.Gauge;
import com.codahale.metrics.MetricRegistry;
import horse.wtf.nzyme.MockNzyme;
import horse.wtf.nzyme.NzymeLeader;
import horse.wtf.nzyme.dot11.MalformedFrameException;
import org.testng.annotations.Test;

public class PrometheusFormatterTest {

    @Test
    public void testFormat() throws Exception, MalformedFrameException {
        NzymeLeader nzyme = new MockNzyme();

        // Create some metrics data.
        nzyme.getMetrics().timer("app.nzyme.test.some-timer_from--somewhere").time().stop();
        nzyme.getMetrics().histogram("app.nzyme.test.some-histo_from--somewhere").update(5);
        nzyme.getMetrics().histogram("app.nzyme.test.some-histo_from--somewhere").update(12);
        nzyme.getMetrics().counter("app.nzyme.test.some-counter_from--somewhere").inc(100);
        nzyme.getMetrics().meter("app.nzyme.test.some-meter_from--somewhere").mark();

        PrometheusFormatter f = new PrometheusFormatter(nzyme);
        System.out.println(f.format());
    }

}