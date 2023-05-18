package app.nzyme.core.monitoring.health.indicators;

import app.nzyme.core.monitoring.health.Indicator;
import app.nzyme.core.monitoring.health.db.IndicatorStatus;
import app.nzyme.core.taps.Bus;
import app.nzyme.core.taps.Channel;
import app.nzyme.core.taps.Tap;
import app.nzyme.core.taps.TapManager;

import java.util.List;
import java.util.Optional;

public class TapBufferIndicator extends Indicator {

    private final TapManager tapManager;

    public TapBufferIndicator(TapManager tapManager) {
        this.tapManager = tapManager;
    }

    @Override
    protected IndicatorStatus doRun() {
        List<Tap> taps = tapManager.findAllTapsOfAllUsers();

        for (Tap tap : taps) {
            Optional<List<Bus>> buses = tapManager.findBusesOfTap(tap.uuid());

            if (buses.isPresent()) {
                for (Bus bus : buses.get()) {
                    Optional<List<Channel>> channels = tapManager.findChannelsOfBus(bus.id());

                    if (channels.isPresent()) {
                        for (Channel channel : channels.get()) {
                            if (channel.watermark() > 75*channel.capacity()/100) {
                                return IndicatorStatus.red(this);
                            }
                        }
                    }
                }
            }
        }

        return IndicatorStatus.green(this);
    }

    @Override
    public String getId() {
        return "tap_buffer";
    }

    @Override
    public String getName() {
        return "Tap Buffer";
    }

}
