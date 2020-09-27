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

package horse.wtf.nzyme.channels;

import com.google.common.base.Joiner;
import com.google.common.collect.Lists;
import com.google.common.io.CharStreams;
import com.google.common.util.concurrent.ThreadFactoryBuilder;
import horse.wtf.nzyme.dot11.probes.Dot11Probe;
import horse.wtf.nzyme.dot11.probes.Dot11ProbeConfiguration;
import horse.wtf.nzyme.util.Tools;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public class ChannelHopper {

    private static final Logger LOG = LogManager.getLogger(ChannelHopper.class);

    private final Dot11Probe probe;
    private final Dot11ProbeConfiguration probeConfiguration;

    private final List<ChannelSwitchHandler> channelSwitchHandlers;

    private List<Integer> configuredChannels;

    private int currentChannel = 0;
    private int currentChannelIndex = 0;

    public ChannelHopper(Dot11Probe probe, Dot11ProbeConfiguration probeConfiguration) {
        if(probeConfiguration.channels() == null || probeConfiguration.channels().isEmpty()) {
            throw new RuntimeException("Channels empty or NULL. You need to configure at least one channel.");
        }

        this.configuredChannels = probeConfiguration.channels();
        this.channelSwitchHandlers = Lists.newArrayList();

        this.probe = probe;
        this.probeConfiguration = probeConfiguration;
    }

    public void initialize() {
        Executors.newSingleThreadScheduledExecutor(new ThreadFactoryBuilder()
                .setDaemon(true)
                .setNameFormat("channel-hopper-%d")
                .build()
        ).scheduleWithFixedDelay(() -> {
            try {
                List<Integer> channels = new ArrayList<>(configuredChannels);
                if (!this.probe.isInLoop()) {
                    LOG.debug("Not hopping channel. Probe [{}] not in loop.", probeConfiguration.networkInterfaceName());
                    return;
                }

                // Check if we reached end of channel list and recycle to 0 in that case.
                if(this.currentChannelIndex >= channels.size()-1) {
                    this.currentChannelIndex = 0;
                } else {
                    this.currentChannelIndex++;
                }

                int channel = channels.get(this.currentChannelIndex);

                LOG.debug("Configuring [{}] to use channel <{}>", probeConfiguration.networkInterfaceName(), channel);

                changeToChannel(channel);
            }catch(Exception e) {
                LOG.error("Could not hop channel.", e);
            }
        }, 0, probeConfiguration.channelHopInterval(), TimeUnit.SECONDS);
    }

    private void changeToChannel(Integer channel) {
        try {
            int previousChannel = currentChannel;
            String networkInterface = Tools.safeAlphanumericString(probeConfiguration.networkInterfaceName());

            String command = probeConfiguration.channelHopCommand()
                    .replace("{channel}", channel.toString()).replace("{interface}", networkInterface);
            LOG.debug("Executing: [{}]", command);

            Process exec = Runtime.getRuntime().exec(command);
            int returnCode = exec.waitFor();

            String stderr = CharStreams.toString(new InputStreamReader(exec.getErrorStream())).replace("\n", "").replace("\r", "");

            if (returnCode != 0 || !stderr.trim().isEmpty()) {
                if (stderr.contains("no tty present and no askpass program specified")) {
                    stderr = stderr + " (are you running with sudo? It must succeed without STDIN/user input. See README for instructions.)";
                }

                LOG.fatal("Could not configure interface [{}] to use channel <{}>. Return code <{}>, STDERR: [{}]", networkInterface, channel, returnCode, stderr);
            } else {
                currentChannel = channel;
                LOG.debug("Channel change successful.");
                for (ChannelSwitchHandler handler : channelSwitchHandlers) {
                    handler.handle(previousChannel, currentChannel);
                }

            }
        } catch(Exception e) {
            LOG.error("Could not hop to channel <{}>.", channel, e);
        }
    }

    public void setChannels(List<Integer> channels) {
        if (channels.equals(this.configuredChannels)) {
            // No need to update if channels did not change.
            return;
        }

        LOG.info("Updating list of channels to <{}>.", Joiner.on(",").join(channels));
        this.configuredChannels = channels;

        this.currentChannel = 0;
        this.currentChannelIndex = 0;
    }

    public Integer getCurrentChannel() {
        return currentChannel;
    }

    public void onChannelSwitch(ChannelSwitchHandler handler) {
        this.channelSwitchHandlers.add(handler);
    }

    public interface ChannelSwitchHandler {
        void handle(int previousChannel, int newChannel);
    }

}
