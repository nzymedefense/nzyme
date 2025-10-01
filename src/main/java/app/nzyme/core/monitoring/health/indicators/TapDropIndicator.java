package app.nzyme.core.monitoring.health.indicators;

import app.nzyme.core.monitoring.health.Indicator;
import app.nzyme.core.monitoring.health.db.IndicatorStatus;
import app.nzyme.core.taps.Capture;
import app.nzyme.core.taps.Tap;
import app.nzyme.core.taps.TapManager;

import java.util.List;

public class TapDropIndicator extends Indicator {

    private final TapManager tapManager;

    public TapDropIndicator(TapManager tapManager) {
        this.tapManager = tapManager;
    }

    @Override
    protected IndicatorStatus doRun() {
        List<Tap> taps = tapManager.findAllTapsOfAllUsers();

        for (Tap tap : taps) {
            for (Capture capture : tapManager.findActiveCapturesOfTap(tap.uuid())) {
                if (capture.droppedBuffer() > 0 || capture.droppedInterface() > 0) {
                    return IndicatorStatus.red(this);
                }
            }
        }

        return IndicatorStatus.green(this);
    }

    @Override
    public String getId() {
        return "tap_drop";
    }

    @Override
    public String getName() {
        return "Tap Drop";
    }

}
