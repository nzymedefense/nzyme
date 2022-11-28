package horse.wtf.nzyme.monitoring.prometheus;

import com.codahale.metrics.*;
import horse.wtf.nzyme.NzymeLeader;

import java.util.Map;

public class PrometheusFormatter {

    private final NzymeLeader nzyme;

    // use reporting template engine

    public PrometheusFormatter(NzymeLeader nzyme) {
        this.nzyme = nzyme;
    }

    @SuppressWarnings("rawtypes")
    public String format() {
        StringBuilder sb = new StringBuilder();

        // Gauges.
        sb.append("# Gauges\n");
        for (Map.Entry<String, Gauge> x : nzyme.getMetrics().getGauges().entrySet()) {
            Gauge gauge = x.getValue();
            if (gauge.getValue() instanceof Number) {
                sb.append(formatKey(x.getKey()))
                        .append(" ")
                        .append(gauge.getValue().toString())
                        .append("\n");
            }
        }

        // Counters.
        sb.append("# Counters\n");
        for (Map.Entry<String, Counter> x : nzyme.getMetrics().getCounters().entrySet()) {
            Counter counter = x.getValue();
            sb.append(formatKey(x.getKey()))
                    .append(" ")
                    .append(counter.getCount())
                    .append("\n");
        }

        // Meters
        sb.append("# Meters\n");
        for (Map.Entry<String, Meter> x : nzyme.getMetrics().getMeters().entrySet()) {
            Meter meter = x.getValue();
            String key = formatKey(x.getKey());

            sb.append(key)
                    .append("_count ")
                    .append(meter.getCount())
                    .append("\n");

            sb.append(key)
                    .append("_mean ")
                    .append(meter.getMeanRate())
                    .append("\n");

            sb.append(key)
                    .append("_rate{ma=\"1\"} ")
                    .append(meter.getOneMinuteRate())
                    .append("\n");

            sb.append(key)
                    .append("_rate{ma=\"5\"} ")
                    .append(meter.getFiveMinuteRate())
                    .append("\n");

            sb.append(key)
                    .append("_rate{ma=\"15\"} ")
                    .append(meter.getFifteenMinuteRate())
                    .append("\n");
        }

        // Histograms
        sb.append("# Histograms\n");
        for (Map.Entry<String, Histogram> x : nzyme.getMetrics().getHistograms().entrySet()) {
            Histogram histogram = x.getValue();
            Snapshot snap = histogram.getSnapshot();
            String key = formatKey(x.getKey());

            sb.append(key)
                    .append("_count ")
                    .append(histogram.getCount())
                    .append("\n");

            sb.append(key)
                    .append("_mean ")
                    .append(snap.getMean())
                    .append("\n");

            sb.append(key)
                    .append("_median ")
                    .append(snap.getMedian())
                    .append("\n");

            sb.append(key)
                    .append("_max ")
                    .append(snap.getMax())
                    .append("\n");

            sb.append(key)
                    .append("_min ")
                    .append(snap.getMin())
                    .append("\n");

            sb.append(key)
                    .append("_stddev ")
                    .append(snap.getStdDev())
                    .append("\n");

            sb.append(key)
                    .append("_dist{percentile=\"75\"} ")
                    .append(snap.get75thPercentile())
                    .append("\n");

            sb.append(key)
                    .append("_dist{percentile=\"95\"} ")
                    .append(snap.get95thPercentile())
                    .append("\n");

            sb.append(key)
                    .append("_dist{percentile=\"98\"} ")
                    .append(snap.get98thPercentile())
                    .append("\n");

            sb.append(key)
                    .append("_dist{percentile=\"99\"} ")
                    .append(snap.get99thPercentile())
                    .append("\n");

            sb.append(key)
                    .append("_dist{percentile=\"999\"} ")
                    .append(snap.get999thPercentile())
                    .append("\n");
        }


        // Timers
        for (Map.Entry<String, Timer> x : nzyme.getMetrics().getTimers().entrySet()) {
            Timer timer = x.getValue();
            Snapshot snap = timer.getSnapshot();
            String key = formatKey(x.getKey());

            sb.append(key)
                    .append("_count ")
                    .append(timer.getCount())
                    .append("\n");

            sb.append(key)
                    .append("_mean_rate ")
                    .append(timer.getMeanRate())
                    .append("\n");

            sb.append(key)
                    .append("_rate{ma=\"1\"} ")
                    .append(timer.getOneMinuteRate())
                    .append("\n");

            sb.append(key)
                    .append("_rate{ma=\"5\"} ")
                    .append(timer.getFiveMinuteRate())
                    .append("\n");

            sb.append(key)
                    .append("_rate{ma=\"15\"} ")
                    .append(timer.getFifteenMinuteRate())
                    .append("\n");

            sb.append(key)
                    .append("_mean ")
                    .append(snap.getMean())
                    .append("\n");

            sb.append(key)
                    .append("_median ")
                    .append(snap.getMedian())
                    .append("\n");

            sb.append(key)
                    .append("_max ")
                    .append(snap.getMax())
                    .append("\n");

            sb.append(key)
                    .append("_min ")
                    .append(snap.getMin())
                    .append("\n");

            sb.append(key)
                    .append("_stddev ")
                    .append(snap.getStdDev())
                    .append("\n");

            sb.append(key)
                    .append("_dist{percentile=\"75\"} ")
                    .append(snap.get75thPercentile())
                    .append("\n");

            sb.append(key)
                    .append("_dist{percentile=\"95\"} ")
                    .append(snap.get95thPercentile())
                    .append("\n");

            sb.append(key)
                    .append("_dist{percentile=\"98\"} ")
                    .append(snap.get98thPercentile())
                    .append("\n");

            sb.append(key)
                    .append("_dist{percentile=\"99\"} ")
                    .append(snap.get99thPercentile())
                    .append("\n");

            sb.append(key)
                    .append("_dist{percentile=\"999\"} ")
                    .append(snap.get999thPercentile())
                    .append("\n");
        }


        return sb.toString();
    }

    private String formatKey(String key) {
        return key
                .replace(".", "_")
                .replace("-", "_")
                .replace("'", "")
                .replaceAll("\\s+", "_")
                .toLowerCase();
    }

}
