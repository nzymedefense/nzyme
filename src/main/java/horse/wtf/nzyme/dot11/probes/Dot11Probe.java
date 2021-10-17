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
import org.joda.time.DateTime;

public abstract class Dot11Probe {

    private final Dot11ProbeConfiguration configuration;

    protected final MetricRegistry metrics;

    public abstract void initialize() throws Dot11ProbeInitializationException;

    public abstract Runnable loop() throws Dot11ProbeInitializationException;
    public abstract boolean isInLoop();
    public abstract Integer getCurrentChannel();
    public abstract Long getTotalFrames();

    public Dot11Probe(Dot11ProbeConfiguration configuration, MetricRegistry metrics) {
        this.configuration = configuration;
        this.metrics = metrics;
    }

    public boolean isActive() {
        DateTime ts = getMostRecentFrameTimestamp();
        if (ts == null) {
            return false;
        }

        return ts.isAfter(DateTime.now().minusSeconds(configuration.maxIdleTimeSeconds()));
    }

    public Dot11ProbeConfiguration getConfiguration() {
        return configuration;
    }

    public String getName() {
        return configuration.probeName();
    }

    public abstract DateTime getMostRecentFrameTimestamp();
}
