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

import com.codahale.metrics.MetricRegistry;
import horse.wtf.nzyme.NzymeLeader;
import horse.wtf.nzyme.alerts.Alert;
import horse.wtf.nzyme.dot11.Dot11FrameInterceptor;
import horse.wtf.nzyme.dot11.Dot11MetaInformation;
import horse.wtf.nzyme.dot11.networks.Networks;
import horse.wtf.nzyme.notifications.Notification;
import horse.wtf.nzyme.statistics.Statistics;
import horse.wtf.nzyme.systemstatus.SystemStatus;

import javax.validation.constraints.NotNull;
import java.util.List;

public abstract class Dot11Probe {

    private final Dot11ProbeConfiguration configuration;
    private final Statistics statistics;

    protected final MetricRegistry metrics;

    public abstract void initialize() throws Dot11ProbeInitializationException;

    public abstract Runnable loop() throws Dot11ProbeInitializationException;
    public abstract boolean isInLoop();
    public abstract Integer getCurrentChannel();
    public abstract Long getTotalFrames();

    public abstract void addFrameInterceptor(Dot11FrameInterceptor interceptor);
    public abstract List<Dot11FrameInterceptor> getInterceptors();

    public Dot11Probe(Dot11ProbeConfiguration configuration, Statistics statistics, MetricRegistry metrics) {
        this.statistics = statistics;
        this.configuration = configuration;
        this.metrics = metrics;
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

}
