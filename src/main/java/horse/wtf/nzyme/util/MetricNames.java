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

package horse.wtf.nzyme.util;

import horse.wtf.nzyme.bandits.engine.ContactIdentifierEngine;
import horse.wtf.nzyme.bandits.trackers.GroundStation;
import horse.wtf.nzyme.dot11.Dot11TaggedParameters;
import horse.wtf.nzyme.dot11.networks.Networks;
import horse.wtf.nzyme.dot11.networks.signalstrength.SignalStrengthTable;
import horse.wtf.nzyme.dot11.probes.Dot11MonitorProbe;
import horse.wtf.nzyme.ouis.OUIManager;
import horse.wtf.nzyme.periodicals.alerting.beaconrate.BeaconRateAnomalyAlertMonitor;
import horse.wtf.nzyme.periodicals.alerting.tracks.SignalTrackMonitor;

import static com.codahale.metrics.MetricRegistry.name;

public class MetricNames {

    // Standard/provided metrics.
    public static final String MEMORY_HEAP_INIT = "mem.heap.init";
    public static final String MEMORY_HEAP_MAX = "mem.heap.max";
    public static final String MEMORY_HEAP_USAGE_PERCENT = "mem.heap.usage";
    public static final String MEMORY_HEAP_USED = "mem.heap.used";
    public static final String MEMORY_NONHEAP_INIT = "mem.non-heap.init";
    public static final String MEMORY_NONHEAP_MAX = "mem.non-heap.max";
    public static final String MEMORY_NONHEAP_USAGE_PERCENT = "mem.non-heap.usage";
    public static final String MEMORY_NONHEAP_USED = "mem.non-heap.used";

    // Custom.
    public static final String FRAME_COUNT = name(Dot11MonitorProbe.class, "frames");
    public static final String FRAME_TIMER = name(Dot11MonitorProbe.class, "timing");
    public static final String OUI_LOOKUP_TIMING = name(OUIManager.class, "lookup-timing");
    public static final String TAGGED_PARAMS_PARSE_TIMING = name(Dot11TaggedParameters.class, "parse-timing");
    public static final String TAGGED_PARAMS_FINGERPRINT_TIMING = name(Dot11TaggedParameters.class, "fingerprint-timing");
    public static final String BEACON_RATE_MONITOR_TIMING = name(BeaconRateAnomalyAlertMonitor.class, "monitor-timing");
    public static final String NETWORKS_SIGNAL_STRENGTH_MEASUREMENTS = name(Networks.class, "signal-strength-measurements");
    public static final String SIGNAL_TABLES_MUTEX_WAIT = name(SignalStrengthTable.class, "mutex-wait");
    public static final String SIGNAL_TRACK_MONITOR_TIMING = name(SignalTrackMonitor.class, "monitor-timing");
    public static final String CONTACT_IDENTIFIER_TIMING = name(ContactIdentifierEngine.class, "timing");
    public static final String GROUNDSTATION_TX = name(GroundStation.class, "tx");
    public static final String GROUNDSTATION_RX = name(GroundStation.class, "rx");
    public static final String GROUNDSTATION_ENCRYPTION_TIMING = name(GroundStation.class, "encryption-timing");
    public static final String GROUNDSTATION_QUEUE_SIZE = name(GroundStation.class, "queue_size");

}
