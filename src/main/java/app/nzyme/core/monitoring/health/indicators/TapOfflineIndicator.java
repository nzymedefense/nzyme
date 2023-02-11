package app.nzyme.core.monitoring.health.indicators;

import app.nzyme.core.monitoring.health.Indicator;
import app.nzyme.core.monitoring.health.db.IndicatorStatus;
import app.nzyme.core.taps.Tap;
import app.nzyme.core.taps.TapManager;
import org.joda.time.DateTime;

import java.util.List;
import java.util.Optional;

public class TapOfflineIndicator extends Indicator {

    private final TapManager tapManager;

    public TapOfflineIndicator(TapManager tapManager) {
        this.tapManager = tapManager;
    }

    @Override
    protected IndicatorStatus doRun() {
        Optional<List<Tap>> taps = tapManager.getTaps();

        if (taps.isEmpty()) {
            return IndicatorStatus.green(this);
        }

        for (Tap tap : taps.get()) {
            if (!tap.deleted() && tap.updatedAt().isBefore(DateTime.now().minusMinutes(2))) {
                return IndicatorStatus.orange(this);
            }
        }

        return IndicatorStatus.green(this);
    }

    @Override
    public String getId() {
        return "tap_offline";
    }

    @Override
    public String getName() {
        return "Tap Offline";
    }

}
