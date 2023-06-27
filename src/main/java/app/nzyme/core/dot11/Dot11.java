package app.nzyme.core.dot11;

import app.nzyme.core.NzymeNode;
import app.nzyme.core.dot11.db.BSSIDSummary;
import app.nzyme.core.dot11.db.SSIDSummary;
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
                                "ARRAY_AGG(DISTINCT(s.ssid)) AS ssids FROM dot11_bssids AS b " +
                                "LEFT JOIN dot11_ssids AS s ON b.id = s.bssid_id " +
                                "LEFT JOIN dot11_fingerprints AS f ON b.id = f.bssid_id " +
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
                                "WHERE created_at > :cutoff AND tap_uuid IN (<taps>)")
                        .bind("cutoff", DateTime.now().minusMinutes(minutes))
                        .bindList("taps", taps)
                        .mapTo(Long.class)
                        .one()
        ) > 0;
    }

    public Optional<BSSIDSummary> findBSSID(String bssid, int minutes, List<UUID> taps) {
        return nzyme.getDatabase().withHandle(handle ->
                handle.createQuery("SELECT b.bssid, AVG(b.signal_strength_average) AS signal_strength_average, " +
                                "MAX(b.created_at) AS last_seen, SUM(b.hidden_ssid_frames) as hidden_ssid_frames, " +
                                "ARRAY_AGG(DISTINCT(COALESCE(s.security_protocol, 'None'))) AS security_protocols, " +
                                "ARRAY_AGG(DISTINCT(f.fingerprint)) AS fingerprints, " +
                                "ARRAY_AGG(DISTINCT(s.ssid)) AS ssids FROM dot11_bssids AS b " +
                                "LEFT JOIN dot11_ssids AS s ON b.id = s.bssid_id " +
                                "LEFT JOIN dot11_fingerprints AS f ON b.id = f.bssid_id " +
                                "WHERE b.created_at > :cutoff AND b.tap_uuid IN (<taps>) " +
                                "GROUP BY b.bssid")
                        .bind("cutoff", DateTime.now().minusMinutes(minutes))
                        .bind("bssid", bssid)
                        .bindList("taps", taps)
                        .mapTo(BSSIDSummary.class)
                        .findOne()
        );
    }

    public List<SSIDSummary> findSSIDsOfBSSID(int minutes, String bssid, List<UUID> taps) {
        return nzyme.getDatabase().withHandle(handle ->
                handle.createQuery("SELECT s.ssid, c.frequency, MAX(s.created_at) AS last_seen, " +
                                "ARRAY_AGG(DISTINCT(COALESCE(s.security_protocol, 'None'))) AS security_protocols, " +
                                "ARRAY_AGG(DISTINCT(s.is_wps)) AS is_wps, " +
                                "AVG(s.signal_strength_average) AS signal_strength_average, " +
                                "SUM(c.stats_bytes) AS total_bytes, SUM(c.stats_frames) AS total_frames " +
                                "FROM dot11_ssids AS s " +
                                "LEFT JOIN dot11_channels AS c on s.id = c.ssid_id " +
                                "WHERE created_at > :cutoff AND bssid = :bssid AND tap_uuid IN (<taps>) " +
                                "GROUP BY s.ssid, c.frequency")
                        .bind("bssid", bssid)
                        .bind("cutoff", DateTime.now().minusMinutes(minutes))
                        .bindList("taps", taps)
                        .mapTo(SSIDSummary.class)
                        .list()
        );
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
