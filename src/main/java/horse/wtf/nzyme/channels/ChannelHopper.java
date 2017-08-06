/*
 *  This file is part of Nzyme.
 *
 *  Nzyme is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  Nzyme is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with Nzyme.  If not, see <http://www.gnu.org/licenses/>.
 */

package horse.wtf.nzyme.channels;

import com.google.common.collect.ImmutableList;
import com.google.common.io.CharStreams;
import com.google.common.util.concurrent.ThreadFactoryBuilder;
import horse.wtf.nzyme.Nzyme;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.InputStreamReader;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public class ChannelHopper {

    private static final Logger LOG = LogManager.getLogger(ChannelHopper.class);

    private final ImmutableList<Integer> channels;
    private final Nzyme nzyme;

    private final Lock lock;

    private int currentChannelIndex = 0;

    public ChannelHopper(Nzyme nzyme, ImmutableList<Integer> channels) {
        if(channels == null || channels.isEmpty()) {
            throw new RuntimeException("Channels empty or NULL. You need to configure at least one channel.");
        }

        this.lock = new Lock();

        this.channels = channels;
        this.nzyme = nzyme;
    }

    public void initialize() {
        Executors.newSingleThreadScheduledExecutor(new ThreadFactoryBuilder()
                .setDaemon(true)
                .setNameFormat("channel-hopper-%d")
                .build()
        ).scheduleWithFixedDelay(() -> {
            try {
                if (!this.nzyme.isInLoop()) {
                    return;
                }

                // Check if we reached end of channel list and recycle to 0 in that case.
                if(this.currentChannelIndex >= this.channels.size()-1) {
                    this.currentChannelIndex = 0;
                } else {
                    this.currentChannelIndex++;
                }

                int channel = this.channels.get(this.currentChannelIndex);

                LOG.debug("Configuring [{}] to use channel <{}>", nzyme.getNetworkInterface(), channel);

                changeToChannel(channel);
            }catch(Exception e) {
                LOG.error("Could not hop channel.", e);
            }
        }, 0, nzyme.getConfiguration().getChannelHopInterval(), TimeUnit.SECONDS);
    }

    public int getCurrentChannel() {
        return this.channels.get(this.currentChannelIndex);
    }

    private void changeToChannel(Integer channel) {
        this.lock.lock();

        try {
            String networkInterface = this.nzyme.getNetworkInterface().replaceAll("[^A-Za-z0-9]", "");

            String command = this.nzyme.getConfiguration().getChannelHopCommand()
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
                LOG.debug("Channel change successful.");
            }
        } catch(Exception e) {
            LOG.error("Could not hop to channel <{}>.", channel, e);
        } finally {
            this.lock.unlock();
        }
    }

    public Lock getLock() {
        return this.lock;
    }

}
