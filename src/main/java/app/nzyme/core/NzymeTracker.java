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

package app.nzyme.core;

import com.codahale.metrics.MetricRegistry;
import com.fasterxml.jackson.databind.ObjectMapper;
import app.nzyme.core.bandits.trackers.GroundStation;
import app.nzyme.core.bandits.trackers.hid.TrackerHID;
import app.nzyme.core.bandits.trackers.trackerlogic.TrackerBanditManager;
import app.nzyme.core.bandits.trackers.trackerlogic.TrackerStateWatchdog;
import app.nzyme.core.configuration.base.BaseConfiguration;
import app.nzyme.core.configuration.tracker.TrackerConfiguration;
import app.nzyme.core.dot11.probes.Dot11Probe;

import java.util.List;

public interface NzymeTracker extends RemoteConnector {

    void initialize();
    void shutdown();

    String getNodeID();

    ObjectMapper getObjectMapper();

    TrackerConfiguration getConfiguration();
    BaseConfiguration getBaseConfiguration();

    GroundStation getGroundStation();
    TrackerBanditManager getBanditManager();
    TrackerStateWatchdog getStateWatchdog();

    List<Dot11Probe> getProbes();

    List<TrackerHID> getHIDs();

    MetricRegistry getMetrics();

}
