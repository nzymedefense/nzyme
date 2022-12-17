package horse.wtf.nzyme.util;

import com.codahale.metrics.*;

public class MetricTools {


    public static Meter getMeter(MetricRegistry metrics, String name) {
        Meter meter = metrics.getMeters().get(name);
        return meter == null ? new Meter() : meter;
    }

    public static Timer getTimer(MetricRegistry metrics, String name) {
        Timer timer = metrics.getTimers().get(name);
        return timer == null ? new Timer() : timer;
    }

    public static Counter getCounter(MetricRegistry metrics, String name) {
        Counter counter = metrics.getCounters().get(name);
        return counter == null ? new Counter() : counter;
    }

    public static Gauge getGauge(MetricRegistry metrics, String name) {
        Gauge gauge = metrics.getGauges().get(name);
        return gauge == null ? (Gauge<String>) () -> "" : gauge;
    }

    public static Histogram getHistogram(MetricRegistry metrics, String name) {
        Histogram histogram = metrics.getHistograms().get(name);
        return histogram == null ? new Histogram(new UniformReservoir()) : histogram;
    }


}
