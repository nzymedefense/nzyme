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

package horse.wtf.nzyme.dot11.probes;

import com.beust.jcommander.internal.Lists;
import com.codahale.metrics.MetricRegistry;
import horse.wtf.nzyme.Nzyme;
import horse.wtf.nzyme.alerts.Alert;
import horse.wtf.nzyme.dot11.Dot11FrameInterceptor;
import horse.wtf.nzyme.dot11.Dot11MetaInformation;
import horse.wtf.nzyme.dot11.networks.Networks;
import horse.wtf.nzyme.notifications.Notification;
import horse.wtf.nzyme.notifications.Uplink;
import horse.wtf.nzyme.notifications.uplinks.graylog.GraylogAddress;
import horse.wtf.nzyme.notifications.uplinks.graylog.GraylogUplink;
import horse.wtf.nzyme.statistics.Statistics;
import horse.wtf.nzyme.systemstatus.SystemStatus;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.validation.constraints.NotNull;
import java.util.List;

public abstract class Dot11Probe {

    private static final Logger LOG = LogManager.getLogger(Dot11Probe.class);

    private final Dot11ProbeConfiguration configuration;
    private final Statistics statistics;
    private final List<Uplink> uplinks;
    private final Nzyme nzyme;

    protected final MetricRegistry metrics;

    public abstract void initialize() throws Dot11ProbeInitializationException;

    public abstract Runnable loop() throws Dot11ProbeInitializationException;
    public abstract boolean isInLoop();
    public abstract Integer getCurrentChannel();
    public abstract Long getTotalFrames();

    public abstract void addFrameInterceptor(Dot11FrameInterceptor interceptor);
    public abstract List<Dot11FrameInterceptor> getInterceptors();

    public abstract void scheduleAction();

    public Dot11Probe(Dot11ProbeConfiguration configuration, Nzyme nzyme) {
        this.nzyme = nzyme;
        this.uplinks = Lists.newArrayList();
        this.statistics = nzyme.getStatistics();
        this.configuration = configuration;
        this.metrics = nzyme.getMetrics();

        if (configuration.graylogAddresses() != null) {
            for (GraylogAddress address : configuration.graylogAddresses()) {
                registerUplink(new GraylogUplink(
                        address.getHost(),
                        address.getPort(),
                        configuration.nzymeId(),
                        configuration.networkInterfaceName())
                );
            }
        }
    }

    public void registerUplink(Uplink uplink) {
        this.uplinks.add(uplink);
    }

    public void notifyUplinks(Notification notification, Dot11MetaInformation meta) {
        for (Uplink uplink : this.uplinks) {
            uplink.notify(notification, meta);
        }
    }

    public void notifyUplinksOfAlert(Alert alert) {
        for (Uplink uplink : this.uplinks) {
            uplink.notifyOfAlert(alert);
        }
    }

    public void addFrameInterceptors(@NotNull List<Dot11FrameInterceptor> interceptors) {
        for (Dot11FrameInterceptor interceptor : interceptors) {
            addFrameInterceptor(interceptor);
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

    public void raiseAlert(Alert alert) {
        this.nzyme.getAlertsService().handle(alert);
    }

    public Networks getNetworks() {
        return nzyme.getNetworks();
    }

    public SystemStatus getSystemStatus() {
        return nzyme.getSystemStatus();
    }

}
