package app.nzyme.core.monitoring.health.indicators;

import app.nzyme.core.monitoring.health.Indicator;
import app.nzyme.core.monitoring.health.db.IndicatorStatus;
import app.nzyme.core.taps.Capture;
import app.nzyme.core.taps.Tap;
import app.nzyme.core.taps.TapManager;

import java.util.List;
import java.util.Optional;

public class TapDropIndicator extends Indicator {

    private final TapManager tapManager;

    public TapDropIndicator(TapManager tapManager) {
        this.tapManager = tapManager;
    }

    @Override
    protected IndicatorStatus doRun() {
        Optional<List<Tap>> taps = tapManager.getTaps();

        if (taps.isPresent()) {
            for (Tap tap : taps.get()) {
                Optional<List<Capture>> capturesOfTap = tapManager.findCapturesOfTap(tap.name());
                if (capturesOfTap.isPresent()) {
                    for (Capture capture : capturesOfTap.get()) {
                        if (capture.droppedBuffer() > 0 || capture.droppedInterface() > 0) {
                            return IndicatorStatus.red(this);
                        }
                    }
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
