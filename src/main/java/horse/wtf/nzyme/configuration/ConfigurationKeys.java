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

package horse.wtf.nzyme.configuration;

public class ConfigurationKeys {

    public static final String GENERAL = "general";
    public static final String PYTHON = "python";
    public static final String ALERTING = "alerting";
    public static final String INTERFACES = "interfaces";
    public static final String DOT11_MONITORS = "802_11_monitors";
    public static final String DOT11_NETWORKS = "802_11_networks";
    public static final String DOT11_ALERTS = "802_11_alerts";
    public static final String GRAYLOG_UPLINKS = "graylog_uplinks";
    public static final String DOT11_TRAPS = "802_11_traps";
    public static final String KNOWN_BANDIT_FINGERPRINTS = "known_bandit_fingerprints";
    public static final String TUNING_PARAMETERS = "tuning_parameters";

    public static final String DEVICE = "device";
    public static final String CHANNELS = "channels";
    public static final String CHANNEL = "channel";
    public static final String HOP_COMMAND = "channel_hop_command";
    public static final String HOP_INTERVAL = "channel_hop_interval";
    public static final String ROLE = "role";
    public static final String ID = "id";
    public static final String ADMIN_PASSWORD_HASH = "admin_password_hash";
    public static final String DATABASE_PATH = "database_path";
    public static final String VERSIONCHECKS = "versionchecks";
    public static final String FETCH_OUIS = "fetch_ouis";
    public static final String PYTHON_PATH = "path";
    public static final String PYTHON_SCRIPT_DIR = "script_directory";
    public static final String PYTHON_SCRIPT_PREFIX = "script_prefix";
    public static final String CLEAN_AFTER_MINUTES = "clean_after_minutes";
    public static final String TRAINING_PERIOD_SECONDS = "training_period_seconds";
    public static final String REST_LISTEN_URI = "rest_listen_uri";
    public static final String HTTP_EXTERNAL_URI = "http_external_uri";
    public static final String USE_TLS = "use_tls";
    public static final String TLS_CERTIFICATE_PATH = "tls_certificate_path";
    public static final String TLS_KEY_PATH = "tls_key_path";
    public static final String SSID = "ssid";
    public static final String SSIDS = "ssids";
    public static final String BSSIDS = "bssids";
    public static final String SECURITY = "security";
    public static final String BSSID = "bssid";
    public static final String FREQUENCY = "frequency";
    public static final String ANTENNA_SIGNAL = "antenna_signal";
    public static final String DEVICE_SENDER = "device_sender";
    public static final String TRAPS = "traps";
    public static final String TYPE = "type";
    public static final String BANDIT_NAMES = "bandit_names";
    public static final String ADDRESS = "address";
    public static final String TRANSMITTER = "transmitter";
    public static final String DELAY_SECONDS = "delay_seconds";
    public static final String FINGERPRINT = "fingerprint";
    public static final String FINGERPRINTS = "fingerprints";
    public static final String BEACON_RATE = "beacon_rate";
    public static final String SIGNAL_QUALITY_TABLE_SIZE_MINUTES = "signal_quality_table_size_minutes";
    public static final String EXPECTED_SIGNAL_DELTA_MODIFIER = "expected_signal_delta_modifier";
    public static final String ANOMALY_ALERT_LOOKBACK_MINUTES = "anomaly_alert_lookback_minutes";
    public static final String ANOMALY_ALERT_TRIGGER_RATIO = "anomaly_alert_trigger_ratio";

}