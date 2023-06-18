package app.nzyme.core.dot11;

import app.nzyme.core.NzymeNode;
import app.nzyme.core.dot11.db.BSSIDEntry;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.joda.time.DateTime;

import java.util.List;
import java.util.UUID;

public class Dot11 {

    private static final Logger LOG = LogManager.getLogger(Dot11.class);

    private final NzymeNode nzyme;

    public Dot11(NzymeNode nzyme) {
        this.nzyme = nzyme;
    }

    public List<BSSIDEntry> findBSSIDs(int minutes, List<UUID> taps) {
        return nzyme.getDatabase().withHandle(handle ->
                handle.createQuery("SELECT bssid, AVG(signal_strength_average) AS signal_strength_average, " +
                                "MAX(created_at) AS last_seen, SUM(hidden_ssid_frames) as hidden_ssid_frames " +
                                "FROM dot11_bssids WHERE created_at > :cutoff AND tap_uuid IN (<taps>) " +
                                "GROUP BY bssid")
                        .bind("cutoff", DateTime.now().minusMinutes(minutes))
                        .bindList("taps", taps)
                        .mapTo(BSSIDEntry.class)
                        .list()
        );
    }

    public List<String> findFingerprintsOfBSSID(int minutes, String bssid) {
        return nzyme.getDatabase().withHandle(handle ->
                handle.createQuery("SELECT DISTINCT(fingerprint) FROM dot11_fingerprints " +
                                "WHERE created_at > :cutoff AND bssid = :bssid")
                        .bind("bssid", bssid)
                        .bind("cutoff", DateTime.now().minusMinutes(minutes))
                        .mapTo(String.class)
                        .list()
        );
    }

    public List<String> findAdvertisedSSIDNamesOfBSSID(int minutes, String bssid) {
        return nzyme.getDatabase().withHandle(handle ->
                handle.createQuery("SELECT DISTINCT(ssid) FROM dot11_ssids " +
                                "WHERE created_at > :cutoff AND bssid = :bssid")
                        .bind("bssid", bssid)
                        .bind("cutoff", DateTime.now().minusMinutes(minutes))
                        .mapTo(String.class)
                        .list()
        );
    }

}
