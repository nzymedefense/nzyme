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

public class Dot11MockProbe extends Dot11Probe {

    public Dot11MockProbe(Dot11ProbeConfiguration configuration, MetricRegistry metrics) {
        super(configuration, metrics);
    }

    @Override
    public DateTime getMostRecentFrameTimestamp() {
        return null;
    }

    @Override
    public void initialize() throws Dot11ProbeInitializationException {

    }

    @Override
    public Runnable loop() throws Dot11ProbeInitializationException {
        return () -> { /* noop */ };
    }

    @Override
    public boolean isInLoop() {
        return false;
    }

    @Override
    public Integer getCurrentChannel() {
        return null;
    }

    @Override
    public Long getTotalFrames() {
        return -1L;
    }

}
