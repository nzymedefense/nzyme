/*
 * This file is part of nzyme.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the Server Side Public License, version 1,
 * as published by MongoDB, Inc.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * Server Side Public License for more details.
 *
 * You should have received a copy of the Server Side Public License
 * along with this program. If not, see
 * <http://www.mongodb.com/licensing/server-side-public-license>.
 */

package horse.wtf.nzyme.dot11.probes;

import com.codahale.metrics.MetricRegistry;
import horse.wtf.nzyme.dot11.Dot11FrameInterceptor;
import horse.wtf.nzyme.statistics.Statistics;
import org.joda.time.DateTime;

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

    public boolean isActive() {
        DateTime ts = getMostRecentFrameTimestamp();
        if (ts == null) {
            return false;
        }

        return ts.isAfter(DateTime.now().minusMinutes(1));
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

    public abstract DateTime getMostRecentFrameTimestamp();
}
