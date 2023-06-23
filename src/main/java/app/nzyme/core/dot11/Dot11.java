package app.nzyme.core.dot11;

import app.nzyme.core.NzymeNode;
import app.nzyme.core.dot11.db.BSSIDSummary;
import app.nzyme.core.dot11.db.SSIDSummary;
import org.joda.time.DateTime;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public class Dot11 {

    private final NzymeNode nzyme;

    public Dot11(NzymeNode nzyme) {
        this.nzyme = nzyme;
    }

    public List<BSSIDSummary> findBSSIDs(int minutes, List<UUID> taps) {
        return nzyme.getDatabase().withHandle(handle ->
                handle.createQuery("SELECT b.bssid, AVG(b.signal_strength_average) AS signal_strength_average, " +
                                "MAX(b.created_at) AS last_seen, SUM(b.hidden_ssid_frames) as hidden_ssid_frames, " +
                                "ARRAY_AGG(DISTINCT(COALESCE(s.security_protocol, 'None'))) AS security_protocols, " +
                                "ARRAY_AGG(DISTINCT(s.ssid)) AS ssids FROM dot11_bssids AS b " +
                                "LEFT JOIN dot11_ssids AS s ON b.id = s.bssid_id " +
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
                                "ARRAY_AGG(DISTINCT(s.ssid)) AS ssids FROM dot11_bssids AS b " +
                                "LEFT JOIN dot11_ssids AS s ON b.id = s.bssid_id " +
                                "WHERE b.created_at > :cutoff AND b.tap_uuid IN (<taps>) " +
                                "GROUP BY b.bssid")
                        .bind("cutoff", DateTime.now().minusMinutes(minutes))
                        .bind("bssid", bssid)
                        .bindList("taps", taps)
                        .mapTo(BSSIDSummary.class)
                        .findOne()
        );
    }

    public List<String> findFingerprintsOfBSSID(int minutes, String bssid, List<UUID> taps) {
        return nzyme.getDatabase().withHandle(handle ->
                handle.createQuery("SELECT DISTINCT(fingerprint) FROM dot11_fingerprints " +
                                "WHERE created_at > :cutoff AND bssid = :bssid AND tap_uuid IN (<taps>)")
                        .bind("bssid", bssid)
                        .bind("cutoff", DateTime.now().minusMinutes(minutes))
                        .bindList("taps", taps)
                        .mapTo(String.class)
                        .list()
        );
    }

    public List<SSIDSummary> findSSIDsOfBSSID(int minutes, String bssid, List<UUID> taps) {
        return nzyme.getDatabase().withHandle(handle ->
                handle.createQuery("SELECT ssid, MAX(created_at) AS last_seen, " +
                                "ARRAY_AGG(DISTINCT(COALESCE(security_protocol, 'None'))) AS security_protocols, " +
                                "ARRAY_AGG(DISTINCT(is_wps)) AS is_wps, " +
                                "AVG(signal_strength_average) AS signal_strength_average FROM dot11_ssids " +
                                "WHERE created_at > :cutoff AND bssid = :bssid AND tap_uuid IN (<taps>) " +
                                "GROUP BY ssid")
                        .bind("bssid", bssid)
                        .bind("cutoff", DateTime.now().minusMinutes(minutes))
                        .bindList("taps", taps)
                        .mapTo(SSIDSummary.class)
                        .list()
        );
    }

}
