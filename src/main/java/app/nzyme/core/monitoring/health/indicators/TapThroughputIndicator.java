package app.nzyme.core.monitoring.health.indicators;

import app.nzyme.core.monitoring.health.Indicator;
import app.nzyme.core.monitoring.health.db.IndicatorStatus;
import app.nzyme.core.taps.Tap;
import app.nzyme.core.taps.TapManager;
import app.nzyme.core.taps.db.metrics.TapMetrics;
import app.nzyme.core.taps.db.metrics.TapMetricsGauge;
import com.google.common.math.DoubleMath;
import org.joda.time.DateTime;

import java.util.List;

public class TapThroughputIndicator extends Indicator {

    private final TapManager tapManager;

    public TapThroughputIndicator(TapManager tapManager) {
        this.tapManager = tapManager;
    }

    @Override
    protected IndicatorStatus doRun() {
        List<Tap> taps = tapManager.findAllTapsOfAllUsers();

        for (Tap tap : taps) {
            if (tap.lastReport() == null || tap.lastReport().isBefore(DateTime.now().minusMinutes(2))) {
                continue;
            }

            for (TapMetricsGauge gauge : tapManager.findGaugesOfTap(tap.uuid())) {
                if (gauge.metricName().equals("system.captures.throughput_bit_sec")) {
                    // Oh god why is it Double
                    if (DoubleMath.fuzzyEquals(gauge.metricValue(), 0, 0.01)) {
                        return IndicatorStatus.orange(this);
                    }
                }
            }
        }

        return IndicatorStatus.green(this);
    }

    @Override
    public String getId() {
        return "tap_tpx";
    }

    @Override
    public String getName() {
        return "Tap TPX";
    }

}
