package horse.wtf.nzyme.monitoring.prometheus;

import com.codahale.metrics.*;
import horse.wtf.nzyme.NzymeLeader;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.math.BigDecimal;
import java.util.Map;
import java.util.Set;

public class PrometheusFormatter {

    private static final Logger LOG = LogManager.getLogger(PrometheusFormatter.class);

    private final NzymeLeader nzyme;

    public PrometheusFormatter(NzymeLeader nzyme) {
        this.nzyme = nzyme;
    }

    // TODO apply number parsing to all nunbers.

    @SuppressWarnings("rawtypes")
    public String format() {
        StringBuilder sb = new StringBuilder();

        // Gauges.
        sb.append("# Gauges\n");
        for (Map.Entry<String, Gauge> x : nzyme.getMetrics().getGauges().entrySet()) {
            try {
                Gauge gauge = x.getValue();
                String printableValue;

                if (gauge.getValue() instanceof Integer) {
                    printableValue = String.valueOf(gauge.getValue());
                } else if (gauge.getValue() instanceof Long) {
                    printableValue = BigDecimal.valueOf((Long) gauge.getValue()).toPlainString();
                } else if (gauge.getValue() instanceof Double) {
                    Double dVal = (Double) gauge.getValue();
                    if (dVal.isNaN() || dVal.isInfinite()) {
                        continue;
                    }

                    printableValue = BigDecimal.valueOf(dVal).toPlainString();
                } else if (gauge.getValue() instanceof String || gauge.getValue() instanceof Set) {
                    LOG.debug("Skipping gauge [{}] of type [{}].",
                            x.getKey(), gauge.getValue().getClass().getCanonicalName());
                    continue;
                } else {
                    LOG.warn("Unknown gauge value type [{}] of [{}]. Skipping.",
                            gauge.getValue().getClass().getCanonicalName(), x.getKey());
                    continue;
                }

                sb.append(formatKey(x.getKey()))
                        .append(" ")
                        .append(printableValue)
                        .append("\n");
            } catch(Exception e) {
                LOG.error("Could not process gauge [{}]. Skipping. Value was: [{}].",
                        x.getKey(), x.getValue().getValue().toString(), e);
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
        sb.append("# Timers\n");
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
