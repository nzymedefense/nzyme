package app.nzyme.core.dot11;

import app.nzyme.core.NzymeNode;
import app.nzyme.core.dot11.db.*;
import com.google.common.collect.Maps;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.joda.time.DateTime;

import java.util.*;

public class Dot11 {

    private static final Logger LOG = LogManager.getLogger(Dot11.class);

    private final NzymeNode nzyme;

    public Dot11(NzymeNode nzyme) {
        this.nzyme = nzyme;
    }

    public List<BSSIDSummary> findBSSIDs(int minutes, List<UUID> taps) {
        return nzyme.getDatabase().withHandle(handle ->
                handle.createQuery("SELECT b.bssid, AVG(b.signal_strength_average) AS signal_strength_average, " +
                                "MAX(b.created_at) AS last_seen, SUM(b.hidden_ssid_frames) as hidden_ssid_frames, " +
                                "ARRAY_AGG(DISTINCT(COALESCE(s.security_protocol, 'None'))) AS security_protocols, " +
                                "ARRAY_AGG(DISTINCT(f.fingerprint)) AS fingerprints, " +
                                "ARRAY_AGG(DISTINCT(s.ssid)) AS ssids, " +
                                "ARRAY_AGG(DISTINCT(i.infrastructure_type)) AS infrastructure_types, " +
                                "COUNT(DISTINCT(c.client_mac)) AS client_count " +
                                "FROM dot11_bssids AS b " +
                                "LEFT JOIN dot11_ssids AS s ON b.id = s.bssid_id " +
                                "LEFT JOIN dot11_fingerprints AS f ON b.id = f.bssid_id " +
                                "LEFT JOIN dot11_infrastructure_types AS i on s.id = i.ssid_id " +
                                "LEFT JOIN dot11_bssid_clients AS c on b.id = c.bssid_id " +
                                "WHERE b.created_at > :cutoff AND b.tap_uuid IN (<taps>) " +
                                "GROUP BY b.bssid")
                        .bind("cutoff", DateTime.now().minusMinutes(minutes))
                        .bindList("taps", taps)
                        .mapTo(BSSIDSummary.class)
                        .list()
        );
    }

    public boolean bssidExist(String bssid, int minutes, List<UUID> taps) {
        return nzyme.getDatabase().withHandle(handle ->
                handle.createQuery("SELECT COUNT(*) FROM dot11_bssids " +
                                "WHERE created_at > :cutoff AND tap_uuid IN (<taps>) " +
                                "AND bssid = :bssid")
                        .bind("cutoff", DateTime.now().minusMinutes(minutes))
                        .bindList("taps", taps)
                        .bind("bssid", bssid)
                        .mapTo(Long.class)
                        .one()
        ) > 0;
    }

    public List<SSIDChannelDetails> findSSIDsOfBSSID(int minutes, String bssid, List<UUID> taps) {
        return nzyme.getDatabase().withHandle(handle ->
                handle.createQuery("SELECT s.ssid, c.frequency, MAX(s.created_at) AS last_seen, " +
                                "ARRAY_AGG(DISTINCT(COALESCE(s.security_protocol, 'None'))) AS security_protocols, " +
                                "ARRAY_AGG(DISTINCT(s.is_wps)) AS is_wps, " +
                                "ARRAY_AGG(DISTINCT(i.infrastructure_type)) AS infrastructure_types, " +
                                "AVG(s.signal_strength_average) AS signal_strength_average, " +
                                "SUM(c.stats_bytes) AS total_bytes, SUM(c.stats_frames) AS total_frames " +
                                "FROM dot11_ssids AS s " +
                                "LEFT JOIN dot11_channels AS c on s.id = c.ssid_id " +
                                "LEFT JOIN dot11_infrastructure_types AS i on s.id = i.ssid_id " +
                                "WHERE created_at > :cutoff AND bssid = :bssid AND tap_uuid IN (<taps>) " +
                                "GROUP BY s.ssid, c.frequency")
                        .bind("bssid", bssid)
                        .bind("cutoff", DateTime.now().minusMinutes(minutes))
                        .bindList("taps", taps)
                        .mapTo(SSIDChannelDetails.class)
                        .list()
        );
    }

    public Optional<SSIDDetails> findSSIDDetails(int minutes, String bssid, String ssid, List<UUID> taps) {
        return nzyme.getDatabase().withHandle(handle ->
                handle.createQuery("SELECT s.ssid, MAX(s.created_at) AS last_seen, " +
                                "ARRAY_AGG(DISTINCT(COALESCE(s.security_protocol, 'None'))) AS security_protocols, " +
                                "ARRAY_AGG(DISTINCT(f.fingerprint)) AS fingerprints, " +
                                "ARRAY_AGG(DISTINCT(r.rate)) AS rates, " +
                                "ARRAY_AGG(DISTINCT(s.is_wps)) AS is_wps, ARRAY_AGG(DISTINCT(i.infrastructure_type)) " +
                                "AS infrastructure_types, AVG(s.signal_strength_average) AS signal_strength_average, " +
                                "ARRAY_AGG(DISTINCT(s.security_suites)) AS security_suites, " +
                                "ARRAY_AGG(DISTINCT(cl.client_mac)) AS access_point_clients, " +
                                "SUM(c.stats_bytes) AS total_bytes, SUM(c.stats_frames) AS total_frames " +
                                "FROM dot11_ssids AS s " +
                                "LEFT JOIN dot11_bssids AS b on s.bssid_id = b.id " +
                                "LEFT JOIN dot11_channels AS c on s.id = c.ssid_id " +
                                "LEFT JOIN dot11_infrastructure_types AS i on s.id = i.ssid_id " +
                                "LEFT JOIN dot11_fingerprints AS f on s.id = f.ssid_id " +
                                "LEFT JOIN dot11_rates AS r on s.id = r.ssid_id " +
                                "LEFT JOIN dot11_bssid_clients cl on b.id = cl.bssid_id " +
                                "WHERE s.created_at > :cutoff AND s.bssid = :bssid AND s.ssid = :ssid " +
                                "AND s.tap_uuid IN (<taps>) " +
                                "GROUP BY ssid")
                        .bind("cutoff", DateTime.now().minusMinutes(minutes))
                        .bind("bssid", bssid)
                        .bind("ssid", ssid)
                        .bindList("taps", taps)
                        .mapTo(SSIDDetails.class)
                        .findOne()
        );
    }

    public List<BSSIDAndSSIDCountHistogramEntry> getBSSIDAndSSIDCountHistogram(int minutes, List<UUID> taps) {
        return nzyme.getDatabase().withHandle(handle ->
                handle.createQuery("SELECT COUNT(DISTINCT(b.bssid)) as bssid_count, " +
                                "COUNT(DISTINCT(s.ssid)) as ssid_count, DATE_TRUNC('minute', b.created_at) as bucket " +
                                "FROM dot11_bssids AS b " +
                                "LEFT JOIN dot11_ssids s ON b.id = s.bssid_id " +
                                "WHERE b.created_at > :cutoff AND b.tap_uuid IN (<taps>) " +
                                "GROUP BY bucket ORDER BY bucket DESC")
                        .bind("cutoff", DateTime.now().minusMinutes(minutes))
                        .bindList("taps", taps)
                        .mapTo(BSSIDAndSSIDCountHistogramEntry.class)
                        .list()
        );
    }

    public List<SSIDAdvertisementHistogramEntry> getSSIDAdvertisementHistogram(String bssid, String ssid, int minutes, List<UUID> taps) {
        return nzyme.getDatabase().withHandle(handle ->
                handle.createQuery("SELECT SUM(beacon_advertisements) AS beacons, " +
                                "SUM(proberesp_advertisements) AS proberesponses, " +
                                "DATE_TRUNC('minute', created_at) AS bucket FROM dot11_ssids " +
                                "WHERE created_at > :cutoff AND tap_uuid IN (<taps>) " +
                                "AND bssid = :bssid AND ssid = :ssid " +
                                "GROUP BY bucket ORDER BY bucket DESC")
                        .bind("cutoff", DateTime.now().minusMinutes(minutes))
                        .bindList("taps", taps)
                        .bind("bssid", bssid)
                        .bind("ssid", ssid)
                        .mapTo(SSIDAdvertisementHistogramEntry.class)
                        .list()
        );
    }

    public List<ActiveChannel> getSSIDChannelUsageHistogram(String bssid, String ssid, int minutes, List<UUID> taps) {
        return nzyme.getDatabase().withHandle(handle ->
                handle.createQuery("SELECT c.frequency, sum(c.stats_frames) AS frames, sum(c.stats_bytes) AS bytes " +
                                "FROM dot11_ssids AS s " +
                                "LEFT JOIN dot11_channels c on s.id = c.ssid_id " +
                                "WHERE created_at > :cutoff AND tap_uuid IN (<taps>) AND s.bssid = :bssid " +
                                "AND s.ssid = :ssid GROUP BY c.frequency")
                        .bind("cutoff", DateTime.now().minusMinutes(minutes))
                        .bindList("taps", taps)
                        .bind("bssid", bssid)
                        .bind("ssid", ssid)
                        .mapTo(ActiveChannel.class)
                        .list()
        );
    }

    public List<ChannelHistogramEntry> getSSIDSignalStrengthWaterfall(String bssid,
                                                                      String ssid,
                                                                      int frequency,
                                                                      int minutes,
                                                                      List<UUID> taps) {
        return nzyme.getDatabase().withHandle(handle ->
                handle.createQuery("SELECT DATE_TRUNC('minute', s.created_at) AS bucket, signal_strength, " +
                                "SUM(frame_count) AS frame_count FROM dot11_ssids AS s " +
                                "LEFT JOIN dot11_channel_histograms h on s.id = h.ssid_id " +
                                "WHERE created_at > :cutoff AND s.tap_uuid IN (<taps>) AND bssid = :bssid " +
                                "AND ssid = :ssid AND h.frequency = :frequency " +
                                "GROUP BY bucket, signal_strength ORDER BY bucket DESC")
                        .bind("cutoff", DateTime.now().minusMinutes(minutes))
                        .bindList("taps", taps)
                        .bind("bssid", bssid)
                        .bind("ssid", ssid)
                        .bind("frequency", frequency)
                        .mapTo(ChannelHistogramEntry.class)
                        .list()
        );
    }

    public static String securitySuitesToIdentifier(Dot11SecuritySuiteJson suite) {
        if (suite.groupCipher() == null && suite.pairwiseCiphers() == null && suite.keyManagementModes() == null) {
            return "NONE";
        }
        return suite.groupCipher() + "-" + suite.pairwiseCiphers() + "/" + suite.keyManagementModes();
    }

    private static Map<Integer, Integer> frequencyChannelMap = Maps.newHashMap();

    static {
        // 2.4 GHz band
        for (int i = 1; i <= 14; i++) {
            int frequency = 2407 + i * 5;
            if (i == 14) {
                frequency = 2484;
            }
            frequencyChannelMap.put(frequency, i);
        }

        // 5 GHz band
        for (int i = 36; i <= 64; i += 4) {
            frequencyChannelMap.put(5000 + i * 5, i);
        }
        for (int i = 100; i <= 140; i += 4) {
            frequencyChannelMap.put(5000 + i * 5, i);
        }
        for (int i = 149; i <= 165; i += 4) {
            frequencyChannelMap.put(5000 + i * 5, i);
        }

        // 6 GHz band
        for (int i = 1; i <= 233; i++) {
            frequencyChannelMap.put(5950 + i * 20, i);
        }
    }

    public static int frequencyToChannel(int frequency) {
        return frequencyChannelMap.getOrDefault(frequency, -1);
    }

}
