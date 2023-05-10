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

package app.nzyme.core.util;

import app.nzyme.core.bandits.engine.ContactIdentifierEngine;
import app.nzyme.core.bandits.trackers.GroundStation;
import app.nzyme.core.crypto.Crypto;
import app.nzyme.core.database.DatabaseImpl;
import app.nzyme.core.dot11.Dot11TaggedParameters;
import app.nzyme.core.dot11.networks.Networks;
import app.nzyme.core.dot11.networks.signalstrength.SignalStrengthTable;
import app.nzyme.core.dot11.probes.Dot11MonitorProbe;
import app.nzyme.core.integrations.geoip.GeoIpService;
import app.nzyme.core.ouis.OUIManager;
import app.nzyme.core.periodicals.alerting.beaconrate.BeaconRateAnomalyAlertMonitor;
import app.nzyme.core.periodicals.alerting.tracks.SignalTrackMonitor;
import app.nzyme.core.remote.inputs.RemoteFrameInput;
import app.nzyme.core.rest.interceptors.TapTableSizeInterceptor;
import app.nzyme.core.security.authentication.PasswordHasher;

import static com.codahale.metrics.MetricRegistry.name;

public class MetricNames {

    // Custom.
    public static final String FRAME_COUNT = name(Dot11MonitorProbe.class, "frames");
    public static final String FRAME_TIMER = name(Dot11MonitorProbe.class, "timing");
    public static final String OUI_LOOKUP_TIMING = name(OUIManager.class, "lookup-timing");
    public static final String TAGGED_PARAMS_PARSE_TIMING = name(Dot11TaggedParameters.class, "parse-timing");
    public static final String TAGGED_PARAMS_FINGERPRINT_TIMING = name(Dot11TaggedParameters.class, "fingerprint-timing");
    public static final String BEACON_RATE_MONITOR_TIMING = name(BeaconRateAnomalyAlertMonitor.class, "monitor-timing");
    public static final String SIGNAL_TABLES_MUTEX_WAIT = name(SignalStrengthTable.class, "mutex-wait");
    public static final String SIGNAL_TRACK_MONITOR_TIMING = name(SignalTrackMonitor.class, "monitor-timing");
    public static final String CONTACT_IDENTIFIER_TIMING = name(ContactIdentifierEngine.class, "timing");
    public static final String GROUNDSTATION_TX = name(GroundStation.class, "tx");
    public static final String GROUNDSTATION_RX = name(GroundStation.class, "rx");
    public static final String GROUNDSTATION_ENCRYPTION_TIMING = name(GroundStation.class, "encryption-timing");
    public static final String GROUNDSTATION_QUEUE_SIZE = name(GroundStation.class, "queue_size");
    public static final String REMOTE_FRAMES_RECEIVED = name(RemoteFrameInput.class, "frames-received");
    public static final String REMOTE_FRAMES_TIMING = name(RemoteFrameInput.class, "frame-timing");
    public static final String DATABASE_SIZE = name(DatabaseImpl.class, "size");

    public static final String GEOIP_CACHE_SIZE = name(GeoIpService.class, "cache-size");

    public static final String PGP_ENCRYPTION_TIMING = name(Crypto.class, "encryption-timing");
    public static final String PGP_DECRYPTION_TIMING = name(Crypto.class, "decryption-timing");

    public static final String PASSWORD_HASHING_TIMER = name(PasswordHasher.class, "hashing-timer");

    public static final String TAP_TABLE_REQUEST_SIZES = name(TapTableSizeInterceptor.class, "request_size");

}
