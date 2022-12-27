package app.nzyme.core.monitoring.prometheus;

import com.codahale.metrics.*;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.math.BigDecimal;
import java.util.Map;
import java.util.Set;

public class PrometheusFormatter {

    private static final Logger LOG = LogManager.getLogger(PrometheusFormatter.class);

    private final MetricRegistry metrics;

    public PrometheusFormatter(MetricRegistry metrics) {
        this.metrics = metrics;
    }

    @SuppressWarnings("rawtypes")
    public String format() {
        StringBuilder sb = new StringBuilder();

        // Gauges.
        sb.append("# Gauges\n");
        for (Map.Entry<String, Gauge> x : metrics.getGauges().entrySet()) {
            try {
                Gauge gauge = x.getValue();

                if (gauge.getValue() instanceof String || gauge.getValue() instanceof Set) {
                    LOG.debug("Skipping gauge [{}] of type [{}].",
                            x.getKey(), gauge.getValue().getClass().getCanonicalName());
                    continue;
                }

                sb.append(formatKey(x.getKey()))
                        .append(" ")
                        .append(formatNumber(gauge.getValue()))
                        .append("\n");
            } catch(Exception e) {
                LOG.error("Could not process gauge [{}]. Skipping. Value was: [{}].",
                        x.getKey(), x.getValue().getValue().toString(), e);
            }
        }

        // Counters.
        sb.append("# Counters\n");
        for (Map.Entry<String, Counter> x : metrics.getCounters().entrySet()) {
            Counter counter = x.getValue();
            sb.append(formatKey(x.getKey()))
                    .append(" ")
                    .append(counter.getCount())
                    .append("\n");
        }

        // Meters
        sb.append("# Meters\n");
        for (Map.Entry<String, Meter> x : metrics.getMeters().entrySet()) {
            Meter meter = x.getValue();
            String key = formatKey(x.getKey());

            sb.append(key)
                    .append("_count ")
                    .append(formatNumber(meter.getCount()))
                    .append("\n");

            sb.append(key)
                    .append("_mean_rate ")
                    .append(formatNumber(meter.getMeanRate()))
                    .append("\n");

            sb.append(key)
                    .append("_rate{ma=\"1\"} ")
                    .append(formatNumber(meter.getOneMinuteRate()))
                    .append("\n");

            sb.append(key)
                    .append("_rate{ma=\"5\"} ")
                    .append(formatNumber(meter.getFiveMinuteRate()))
                    .append("\n");

            sb.append(key)
                    .append("_rate{ma=\"15\"} ")
                    .append(formatNumber(meter.getFifteenMinuteRate()))
                    .append("\n");
        }

        // Histograms
        sb.append("# Histograms\n");
        for (Map.Entry<String, Histogram> x : metrics.getHistograms().entrySet()) {
            Histogram histogram = x.getValue();
            Snapshot snap = histogram.getSnapshot();
            String key = formatKey(x.getKey());

            sb.append(key)
                    .append("_count ")
                    .append(formatNumber(histogram.getCount()))
                    .append("\n");

            sb.append(key)
                    .append("_mean ")
                    .append(formatNumber(snap.getMean()))
                    .append("\n");

            sb.append(key)
                    .append("_median ")
                    .append(formatNumber(snap.getMedian()))
                    .append("\n");

            sb.append(key)
                    .append("_max ")
                    .append(formatNumber(snap.getMax()))
                    .append("\n");

            sb.append(key)
                    .append("_min ")
                    .append(formatNumber(snap.getMin()))
                    .append("\n");

            sb.append(key)
                    .append("_stddev ")
                    .append(formatNumber(snap.getStdDev()))
                    .append("\n");

            sb.append(key)
                    .append("_dist{percentile=\"75\"} ")
                    .append(formatNumber(snap.get75thPercentile()))
                    .append("\n");

            sb.append(key)
                    .append("_dist{percentile=\"95\"} ")
                    .append(formatNumber(snap.get95thPercentile()))
                    .append("\n");

            sb.append(key)
                    .append("_dist{percentile=\"98\"} ")
                    .append(formatNumber(snap.get98thPercentile()))
                    .append("\n");

            sb.append(key)
                    .append("_dist{percentile=\"99\"} ")
                    .append(formatNumber(snap.get99thPercentile()))
                    .append("\n");

            sb.append(key)
                    .append("_dist{percentile=\"999\"} ")
                    .append(formatNumber(snap.get999thPercentile()))
                    .append("\n");
        }

        // Timers
        sb.append("# Timers\n");
        for (Map.Entry<String, Timer> x : metrics.getTimers().entrySet()) {
            Timer timer = x.getValue();
            Snapshot snap = timer.getSnapshot();
            String key = formatKey(x.getKey());

            sb.append(key)
                    .append("_count ")
                    .append(formatNumber(timer.getCount()))
                    .append("\n");

            sb.append(key)
                    .append("_mean_rate ")
                    .append(formatNumber(timer.getMeanRate()))
                    .append("\n");

            sb.append(key)
                    .append("_rate{ma=\"1\"} ")
                    .append(formatNumber(timer.getOneMinuteRate()))
                    .append("\n");

            sb.append(key)
                    .append("_rate{ma=\"5\"} ")
                    .append(formatNumber(timer.getFiveMinuteRate()))
                    .append("\n");

            sb.append(key)
                    .append("_rate{ma=\"15\"} ")
                    .append(formatNumber(timer.getFifteenMinuteRate()))
                    .append("\n");

            sb.append(key)
                    .append("_mean_seconds ")
                    .append(formatNumber(snap.getMean()))
                    .append("\n");

            sb.append(key)
                    .append("_median_seconds ")
                    .append(formatNumber(snap.getMedian()))
                    .append("\n");

            sb.append(key)
                    .append("_max_seconds ")
                    .append(formatNumber(snap.getMax()))
                    .append("\n");

            sb.append(key)
                    .append("_min_seconds ")
                    .append(formatNumber(snap.getMin()))
                    .append("\n");

            sb.append(key)
                    .append("_stddev ")
                    .append(formatNumber(snap.getStdDev()))
                    .append("\n");

            sb.append(key)
                    .append("_dist_seconds{percentile=\"75\"} ")
                    .append(formatNumber(snap.get75thPercentile()))
                    .append("\n");

            sb.append(key)
                    .append("_dist_seconds{percentile=\"95\"} ")
                    .append(formatNumber(snap.get95thPercentile()))
                    .append("\n");

            sb.append(key)
                    .append("_dist_seconds{percentile=\"98\"} ")
                    .append(formatNumber(snap.get98thPercentile()))
                    .append("\n");

            sb.append(key)
                    .append("_dist_seconds{percentile=\"99\"} ")
                    .append(formatNumber(snap.get99thPercentile()))
                    .append("\n");

            sb.append(key)
                    .append("_dist_seconds{percentile=\"999\"} ")
                    .append(formatNumber(snap.get999thPercentile()))
                    .append("\n");
        }


        return sb.toString();
    }

    private String formatKey(String key) {
        return key
                .replaceAll("[^A-Za-z0-9\\s]", "_")
                .replace("__", "_")
                .toLowerCase();
    }

    private String formatNumber(Object o) {
        if (o instanceof Integer) {
            return String.valueOf(o);
        } else if (o instanceof Long) {
            return BigDecimal.valueOf((Long) o).toPlainString();
        } else if (o instanceof Double) {
            Double dVal = (Double) o;
            if (dVal.isNaN()) {
                return "NaN";
            }

            if (dVal.isInfinite()) {
                return "+Inf";
            }

            return BigDecimal.valueOf(dVal).toPlainString();
        } else {
            LOG.warn("Unknown gauge value type [{}]. Skipping.", o.getClass().getCanonicalName());
            return "0";
        }
    }

}
