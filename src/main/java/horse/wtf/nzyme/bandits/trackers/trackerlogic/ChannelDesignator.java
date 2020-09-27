/*
 *  This file is part of nzyme.
 *
 *  nzyme is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  nzyme is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with nzyme.  If not, see <http://www.gnu.org/licenses/>.
 */

package horse.wtf.nzyme.bandits.trackers.trackerlogic;

import com.google.common.base.Joiner;
import com.google.common.collect.ImmutableList;
import com.google.common.util.concurrent.ThreadFactoryBuilder;
import horse.wtf.nzyme.dot11.probes.Dot11MonitorProbe;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

public class ChannelDesignator {

    private static final Logger LOG = LogManager.getLogger(ChannelDesignator.class);

    private static final short SWEEP_BREAK = 5;

    public enum DESIGNATION_STATUS {
        LOCKED,
        UNLOCKED,
        SWEEPING
    }

    private final Dot11MonitorProbe probe;

    private final AtomicReference<List<Integer>> designatedChannels;

    private final AtomicBoolean contactDuringCycle;
    private final AtomicReference<DESIGNATION_STATUS> status;

    private short loopCount;

    public ChannelDesignator(Dot11MonitorProbe probe) {
        this.probe = probe;
        this.contactDuringCycle = new AtomicBoolean(false);
        this.status = new AtomicReference<>(DESIGNATION_STATUS.UNLOCKED);

        // Start with the default/configured channels of the underlying probe.
        this.designatedChannels = new AtomicReference<>(new ArrayList<>(probe.getConfiguration().channels()));

        // Periodically designate channels for ChannelHopper.
        Executors.newSingleThreadScheduledExecutor(new ThreadFactoryBuilder()
                .setDaemon(true)
                .setNameFormat("channel-designator-%d")
                .build())
                .scheduleAtFixedRate(this::designate, 0, 30, TimeUnit.SECONDS);

        LOG.info("Channel Designator is online.");
    }

    public void onBanditTrace(int channel) {
        this.contactDuringCycle.set(true);
        if (!this.designatedChannels.get().contains(channel)) {
            this.designatedChannels.get().add(channel);
        }
    }

    public DESIGNATION_STATUS getStatus() {
        return status.get();
    }

    private void designate() {
        boolean skip = false;

        // Is this a sweep run?
        if (loopCount == SWEEP_BREAK-1) {
            loopCount = 0;

            if (this.contactDuringCycle.get()) {
                LOG.info("Starting designator sweep.");
                this.probe.getChannelHopper().setChannels(probe.getConfiguration().channels());
                this.status.set(DESIGNATION_STATUS.SWEEPING);

                skip = true;
            }
        }

        if (!skip) {
            if (contactDuringCycle.get()) {
                // Contacts recorded during cycle. Designate all channels that a contact was made on.
                LOG.info("Contacts recorded during cycle. Designating channels [{}].", Joiner.on(",").join(designatedChannels.get()));
                this.probe.getChannelHopper().setChannels(intersectChannels(new ArrayList<>(designatedChannels.get()), probe.getConfiguration().channels()));

                this.status.set(DESIGNATION_STATUS.LOCKED);
            } else {
                // No contacts during cycle. Designate all configured channels.
                LOG.info("No contacts during cycle. Resuming operations on configured probe channels.");
                this.probe.getChannelHopper().setChannels(probe.getConfiguration().channels());

                this.status.set(DESIGNATION_STATUS.UNLOCKED);
            }
        }

        // Reset for next cycle.
        this.contactDuringCycle.set(false);
        this.designatedChannels.get().clear();
        loopCount++;
    }

    private List<Integer> intersectChannels(List<Integer> partial, List<Integer> complete) {
        ImmutableList.Builder<Integer> result = new ImmutableList.Builder<>();
        for (Integer x : partial) {
            if (complete.contains(x)) {
                result.add(x);
            }
        }

        return result.build();
    }

}
