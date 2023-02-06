package app.nzyme.core.monitoring.health.indicators;

import app.nzyme.core.monitoring.health.Indicator;
import app.nzyme.core.monitoring.health.db.IndicatorStatus;
import app.nzyme.core.taps.TapManager;

public class TapClockIndicator extends Indicator {

    private final TapManager tapManager;

    public TapClockIndicator(TapManager tapManager) {
        this.tapManager = tapManager;
    }

    @Override
    protected IndicatorStatus doRun() {
        return null;
    }

    @Override
    public String getId() {
        return "tap_clock";
    }

    @Override
    public String getName() {
        return "Tap Clock";
    }

}
