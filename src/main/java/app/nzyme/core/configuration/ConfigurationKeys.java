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

package app.nzyme.core.configuration;

public class ConfigurationKeys {

    public static final String GENERAL = "general";
    public static final String ALERTING = "alerting";
    public static final String INTERFACES = "interfaces";
    public static final String DOT11_MONITORS = "802_11_monitors";
    public static final String DOT11_NETWORKS = "802_11_networks";
    public static final String DOT11_ALERTS = "802_11_alerts";
    public static final String GRAYLOG_UPLINKS = "graylog_uplinks";
    public static final String UPLINKS = "uplinks";
    public static final String DOT11_TRAPS = "802_11_traps";
    public static final String UPLINK_DEVICE = "uplink_device";
    public static final String GROUNDSTATION_DEVICE = "groundstation_device";
    public static final String DATA_DIRECTORY = "data_directory";
    public static final String PLUGIN_DIRECTORY = "plugin_directory";
    public static final String CRYPTO_DIRECTORY = "crypto_directory";
    public static final String NTP_SERVER = "ntp_server";

    public static final String REMOTE_INPUT = "remote_input";
    public static final String REPORTING = "reporting";

    public static final String DEVICE = "device";
    public static final String CHANNELS = "channels";
    public static final String CHANNEL = "channel";
    public static final String HOP_COMMAND = "channel_hop_command";
    public static final String HOP_INTERVAL = "channel_hop_interval";
    public static final String SKIP_ENABLE_MONITOR = "skip_enable_monitor";
    public static final String MAX_IDLE_TIME_SECONDS = "max_idle_time_seconds";
    public static final String ROLE = "role";
    public static final String NAME = "name";
    public static final String ADMIN_PASSWORD_HASH = "admin_password_hash";
    public static final String DATABASE_PATH = "database_path";
    public static final String VERSIONCHECKS = "versionchecks";
    public static final String FETCH_OUIS = "fetch_ouis";
    public static final String TRAINING_PERIOD_SECONDS = "training_period_seconds";
    public static final String REST_LISTEN_URI = "rest_listen_uri";
    public static final String HTTP_EXTERNAL_URI = "http_external_uri";
    public static final String SSID = "ssid";
    public static final String SSIDS = "ssids";
    public static final String BSSIDS = "bssids";
    public static final String SECURITY = "security";
    public static final String BSSID = "bssid";
    public static final String FREQUENCY = "frequency";
    public static final String ANTENNA_SIGNAL = "antenna_signal";
    public static final String TRAP = "trap";
    public static final String TYPE = "type";
    public static final String ADDRESS = "address";
    public static final String TRANSMITTER = "transmitter";
    public static final String DELAY_SECONDS = "delay_seconds";
    public static final String DELAY_MILLISECONDS = "delay_milliseconds";
    public static final String FINGERPRINTS = "fingerprints";
    public static final String FINGERPRINT = "fingerprint";
    public static final String TRACK_DETECTOR = "track_detector";
    public static final String FRAME_THRESHOLD = "frame_threshold";
    public static final String GAP_THRESHOLD = "gap_threshold";
    public static final String SIGNAL_CENTERLINE_JITTER = "signal_centerline_jitter";
    public static final String BEACON_RATE = "beacon_rate";
    public static final String CALLBACKS = "callbacks";
    public static final String HIDS = "hids";
    public static final String CONFIGURATION = "configuration";
    public static final String HOST = "host";
    public static final String PORT = "port";
    public static final String FORWARDERS = "forwarders";
    public static final String EMAIL = "email";
    public static final String WHERE = "alerting.callbacks.[email]";
    public static final String TRANSPORT_STRATEGY = "transport_strategy";
    public static final String USERNAME = "username";
    public static final String PASSWORD = "password";
    public static final String RECIPIENTS = "recipients";
    public static final String FROM = "from";
    public static final String SUBJECT_PREFIX = "subject_prefix";
    public static final String PATH = "path";
    public static final String DEAUTH_MONITOR = "deauth_monitor";
    public static final String GLOBAL_THRESHOLD = "global_threshold";

    public static final String PARAMETERS = "parameters";
    public static final String SERIAL_PORT = "serial_port";
    public static final String ENCRYPTION_KEY = "encryption_key";

    public static final String ENABLED = "enabled";
    public static final String TRACKER_NAME = "tracker_name";
    public static final String ANONYMIZE = "anonymize";

}