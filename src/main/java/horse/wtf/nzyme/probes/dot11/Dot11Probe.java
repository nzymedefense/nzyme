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

package horse.wtf.nzyme.probes.dot11;

import com.beust.jcommander.internal.Lists;
import horse.wtf.nzyme.dot11.Dot11FrameInterceptor;
import horse.wtf.nzyme.dot11.Dot11MetaInformation;
import horse.wtf.nzyme.notifications.Notification;
import horse.wtf.nzyme.notifications.Uplink;
import horse.wtf.nzyme.notifications.uplinks.graylog.GraylogAddress;
import horse.wtf.nzyme.notifications.uplinks.graylog.GraylogUplink;
import horse.wtf.nzyme.statistics.Statistics;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.List;

public abstract class Dot11Probe {

    private static final Logger LOG = LogManager.getLogger(Dot11Probe.class);

    private final Dot11ProbeConfiguration configuration;
    private final Statistics statistics;
    private final List<Uplink> uplinks;

    public abstract Runnable loop() throws Dot11ProbeInitializationException;
    public abstract boolean isInLoop();

    public abstract void addFrameInterceptor(Dot11FrameInterceptor interceptor);
    public abstract void scheduleAction();

    public Dot11Probe(Dot11ProbeConfiguration configuration, Statistics statistics) {
        this.uplinks = Lists.newArrayList();
        this.statistics = statistics;
        this.configuration = configuration;

        if (configuration.graylogAddresses() == null || configuration.graylogAddresses().isEmpty()) {
            LOG.warn("No Graylog uplinks configured for probe [{}]. Consider adding a STDOUT uplink for quick local testing.", getName());
        } else {
            for (GraylogAddress address : configuration.graylogAddresses()) {
                this.uplinks.add(new GraylogUplink(
                        address.getHost(),
                        address.getPort(),
                        configuration.nzymeId(),
                        configuration.networkInterfaceName())
                );
            }
        }
    }

    public void notifyUplinks(Notification notification, Dot11MetaInformation meta) {
        for (Uplink uplink : this.uplinks) {
            uplink.notify(notification, meta);
        }
    }

    public Statistics getStatistics() {
        return statistics;
    }

    public Dot11ProbeConfiguration getConfiguration() {
        return configuration;
    }

    public String getName() {
        return configuration.probeName();
    }

}
