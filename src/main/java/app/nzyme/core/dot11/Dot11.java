package app.nzyme.core.dot11;

import app.nzyme.core.NzymeNode;
import app.nzyme.core.database.OrderDirection;
import app.nzyme.core.dot11.db.*;
import app.nzyme.core.dot11.db.monitoring.*;
import app.nzyme.core.rest.resources.taps.reports.tables.dot11.Dot11SecurityInformationReport;
import app.nzyme.core.rest.responses.dot11.clients.ConnectedBSSID;
import app.nzyme.core.util.Tools;
import com.google.common.base.Joiner;
import com.google.common.base.Strings;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import org.jdbi.v3.core.statement.Query;
import org.jdbi.v3.core.statement.Update;
import org.joda.time.DateTime;

import javax.annotation.Nullable;
import java.util.*;

public class Dot11 {

    /*
     * JDBI bindList() does not work with empty lists. In some cases, we must expect an empty list and will pass
     * this list instead to avoid maintaining multiple queries. The random value in this list will not match, just
     * like an empty list. Computers!
     */
    private static final List<String> noValuesBindList = new ArrayList<>(){{
        add("GYGzDTnSDLgJs9rMY8ZXj0EVwDBw2lZl");
    }};

    private final NzymeNode nzyme;

    public enum ClientOrderColumn {

        LAST_SEEN("last_seen");

        private final String columnName;

        ClientOrderColumn(String columnName) {
            this.columnName = columnName;
        }

        public String getColumnName() {
            return columnName;
        }

    }

    public Dot11(NzymeNode nzyme) {
        this.nzyme = nzyme;
    }

    public List<String> findAllSSIDNames(List<UUID> taps) {
        if (taps.isEmpty()) {
            return Collections.emptyList();
        }

        return nzyme.getDatabase().withHandle(handle ->
                handle.createQuery("SELECT DISTINCT(ssid) FROM dot11_ssids " +
                                "WHERE tap_uuid IN (<taps>) ORDER BY ssid ASC")
                        .bindList("taps", taps)
                        .mapTo(String.class)
                        .list()
        );
    }

    public List<BSSIDSummary> findBSSIDs(int minutes, List<UUID> taps) {
        if (taps.isEmpty()) {
            return Collections.emptyList();
        }

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

    public List<String> findAllBSSIDsAdvertisingSSID(int minutes, String ssid, List<UUID> taps) {
        return nzyme.getDatabase().withHandle(handle ->
                handle.createQuery("SELECT DISTINCT(b.bssid) FROM dot11_bssids AS b " +
                                "LEFT JOIN dot11_ssids AS s ON b.id = s.bssid_id " +
                                "WHERE s.ssid = :ssid AND b.created_at > :cutoff AND b.tap_uuid IN (<taps>)")
                        .bind("ssid", ssid)
                        .bind("cutoff", DateTime.now().minusMinutes(minutes))
                        .bindList("taps", taps)
                        .mapTo(String.class)
                        .list()
        );
    }

    public List<String> findFingerprintsOfBSSID(int minutes, String bssid, List<UUID> taps) {
        return nzyme.getDatabase().withHandle(handle ->
                handle.createQuery("SELECT DISTINCT(f.fingerprint) FROM dot11_bssids AS b " +
                                "LEFT JOIN dot11_fingerprints AS f ON b.id = f.bssid_id " +
                                "WHERE b.bssid = :bssid AND b.created_at > :cutoff AND b.tap_uuid IN (<taps>)")
                        .bind("bssid", bssid)
                        .bind("cutoff", DateTime.now().minusMinutes(minutes))
                        .bindList("taps", taps)
                        .mapTo(String.class)
                        .list()
        );
    }

    public List<String> findSecuritySuitesOfSSIDOfBSSID(int minutes, String bssid, String ssid, List<UUID> taps) {
        return nzyme.getDatabase().withHandle(handle ->
                handle.createQuery("SELECT DISTINCT(s.security_suites) FROM dot11_bssids AS b " +
                                "LEFT JOIN dot11_ssids AS s ON b.id = s.bssid_id " +
                                "WHERE b.bssid = :bssid AND s.ssid = :ssid AND b.created_at > :cutoff " +
                                "AND b.tap_uuid IN (<taps>)")
                        .bind("bssid", bssid)
                        .bind("ssid", ssid)
                        .bind("cutoff", DateTime.now().minusMinutes(minutes))
                        .bindList("taps", taps)
                        .mapTo(String.class)
                        .list()
        );
    }

    public List<Integer> findChannelsOfSSIDOfBSSID(int minutes, String bssid, String ssid, List<UUID> taps) {
        return nzyme.getDatabase().withHandle(handle ->
                handle.createQuery("SELECT DISTINCT(c.frequency) FROM dot11_bssids AS b " +
                                "LEFT JOIN dot11_ssids AS s ON b.id = s.bssid_id " +
                                "LEFT JOIN dot11_channels AS c ON s.id = c.ssid_id " +
                                "WHERE b.bssid = :bssid AND s.ssid = :ssid AND b.created_at > :cutoff " +
                                "AND b.tap_uuid IN (<taps>) AND c.frequency IS NOT NULL")
                        .bind("bssid", bssid)
                        .bind("ssid", ssid)
                        .bind("cutoff", DateTime.now().minusMinutes(minutes))
                        .bindList("taps", taps)
                        .mapTo(Integer.class)
                        .list()
        );
    }

    public List<BSSIDWithTap> findAllBSSIDSOfAllTenantsWithFingerprint(int minutes, String fingerprint) {
        return nzyme.getDatabase().withHandle(handle ->
                handle.createQuery("SELECT b.bssid AS bssid, b.tap_uuid AS tap_uuid, " +
                                "AVG(b.signal_strength_average) AS signal_strength " +
                                "FROM dot11_fingerprints fp " +
                                "LEFT JOIN dot11_bssids AS b ON b.id = fp.bssid_id " +
                                "WHERE b.created_at > :cutoff AND fp.fingerprint = :fingerprint " +
                                "AND b.bssid IS NOT NULL " +
                                "GROUP BY b.bssid, b.tap_uuid")
                        .bind("cutoff", DateTime.now().minusMinutes(minutes))
                        .bind("fingerprint", fingerprint)
                        .mapTo(BSSIDWithTap.class)
                        .list()
        );
    }

    public boolean bssidExist(String bssid, int minutes, List<UUID> taps) {
        if (taps.isEmpty()) {
            return false;
        }

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
        if (taps.isEmpty()) {
            return Collections.emptyList();
        }

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
        if (taps.isEmpty()) {
            return Optional.empty();
        }

        return nzyme.getDatabase().withHandle(handle ->
                handle.createQuery("SELECT s.ssid, MAX(s.created_at) AS last_seen, " +
                                "ARRAY_AGG(DISTINCT(COALESCE(s.security_protocol, 'None'))) AS security_protocols, " +
                                "ARRAY_AGG(DISTINCT(f.fingerprint)) AS fingerprints, " +
                                "ARRAY_AGG(DISTINCT(r.rate)) AS rates, " +
                                "ARRAY_AGG(DISTINCT(s.is_wps)) AS is_wps, ARRAY_AGG(DISTINCT(i.infrastructure_type)) " +
                                "AS infrastructure_types, AVG(s.signal_strength_average) AS signal_strength_average, " +
                                "ARRAY_AGG(DISTINCT(s.security_suites)) AS security_suites, " +
                                "ARRAY_AGG(DISTINCT(cl.client_mac)) AS access_point_clients, " +
                                "ARRAY_AGG(DISTINCT(c.frequency)) AS frequencies, " +
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
        if (taps.isEmpty()) {
            return Collections.emptyList();
        }

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
        if (taps.isEmpty()) {
            return Collections.emptyList();
        }

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
        if (taps.isEmpty()) {
            return Collections.emptyList();
        }

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
        if (taps.isEmpty()) {
            return Collections.emptyList();
        }

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

    public long countBSSIDClients(int minutes, List<UUID> taps) {
        if (taps.isEmpty()) {
            return 0;
        }

        return nzyme.getDatabase().withHandle(handle ->
                handle.createQuery("SELECT COUNT(DISTINCT(c.client_mac)) " +
                                "FROM dot11_bssids AS b " +
                                "LEFT JOIN dot11_bssid_clients c on b.id = c.bssid_id " +
                                "WHERE b.created_at > :cutoff AND b.tap_uuid IN (<taps>)")
                        .bind("cutoff", DateTime.now().minusMinutes(minutes))
                        .bindList("taps", taps)
                        .mapTo(Long.class)
                        .one()
        );
    }

    public List<ConnectedClientDetails> findBSSIDClients(int minutes,
                                                         List<UUID> taps,
                                                         int limit,
                                                         int offset,
                                                         ClientOrderColumn orderColumn,
                                                         OrderDirection orderDirection) {
        if (taps.isEmpty()) {
            return Collections.emptyList();
        }

        return nzyme.getDatabase().withHandle(handle ->
                handle.createQuery("SELECT b.bssid AS bssid, c.client_mac AS client_mac, " +
                                "MAX(b.created_at) AS last_seen " +
                                "FROM dot11_bssids AS b " +
                                "LEFT JOIN dot11_bssid_clients c on b.id = c.bssid_id " +
                                "WHERE b.created_at > :cutoff AND b.tap_uuid IN (<taps>) " +
                                "GROUP BY c.client_mac, b.bssid " +
                                "HAVING c.client_mac IS NOT NULL " +
                                "ORDER BY <order_column> <order_direction> " +
                                "LIMIT :limit OFFSET :offset")
                        .bind("cutoff", DateTime.now().minusMinutes(minutes))
                        .bindList("taps", taps)
                        .define("order_column", orderColumn.getColumnName())
                        .define("order_direction", orderDirection)
                        .bind("limit", limit)
                        .bind("offset", offset)
                        .mapTo(ConnectedClientDetails.class)
                        .list()
        );
    }

    public List<String> findMacAddressesOfAllBSSIDClients(int minutes, List<UUID> taps) {
        if (taps.isEmpty()) {
            return Collections.emptyList();
        }

        return nzyme.getDatabase().withHandle(handle ->
                handle.createQuery("SELECT DISTINCT(b.bssid) AS bssid " +
                                "FROM dot11_bssids AS b " +
                                "WHERE b.created_at > :cutoff AND b.tap_uuid IN (<taps>)")
                        .bind("cutoff", DateTime.now().minusMinutes(minutes))
                        .bindList("taps", taps)
                        .mapTo(String.class)
                        .list()
        );
    }

    public List<String> findProbeRequestsOfClient(String clientMac,
                                                  List<UUID> taps) {
        if (taps.isEmpty()) {
            return Collections.emptyList();
        }

        return nzyme.getDatabase().withHandle(handle ->
                handle.createQuery("SELECT DISTINCT(s.ssid) " +
                                "FROM dot11_clients AS c " +
                                "LEFT JOIN dot11_client_probereq_ssids s on c.id = s.client_id " +
                                "WHERE c.tap_uuid IN (<taps>) " +
                                "AND c.client_mac = :client_mac AND s.ssid IS NOT NULL")
                        .bindList("taps", taps)
                        .bind("client_mac", clientMac)
                        .mapTo(String.class)
                        .list()
        );
    }

    public long countClients(int minutes, List<UUID> taps) {
        if (taps.isEmpty()) {
            return 0;
        }

        return nzyme.getDatabase().withHandle(handle ->
                handle.createQuery("SELECT COUNT(DISTINCT(c.client_mac)) " +
                                "FROM dot11_clients AS c " +
                                "LEFT JOIN dot11_client_probereq_ssids AS pr on c.id = pr.client_id " +
                                "WHERE c.created_at > :cutoff AND c.tap_uuid IN (<taps>)")
                        .bind("cutoff", DateTime.now().minusMinutes(minutes))
                        .bindList("taps", taps)
                        .mapTo(Long.class)
                        .one()
        );
    }

    public List<DisconnectedClientDetails> findClients(int minutes,
                                                       List<UUID> taps,
                                                       List<String> excludeClientMacs,
                                                       int limit,
                                                       int offset,
                                                       ClientOrderColumn orderColumn,
                                                       OrderDirection orderDirection) {
        if (taps.isEmpty()) {
            return Collections.emptyList();
        }

        return nzyme.getDatabase().withHandle(handle ->
                handle.createQuery("SELECT c.client_mac, MAX(created_at) AS last_seen, " +
                                "ARRAY_AGG(DISTINCT(pr.ssid)) AS probe_requests " +
                                "FROM dot11_clients AS c " +
                                "LEFT JOIN dot11_client_probereq_ssids AS pr on c.id = pr.client_id " +
                                "WHERE c.created_at > :cutoff AND c.tap_uuid IN (<taps>) " +
                                "AND NOT c.client_mac IN (<exclude_client_macs>) " +
                                "GROUP BY c.client_mac " +
                                "ORDER BY <order_column> <order_direction> " +
                                "LIMIT :limit OFFSET :offset")
                        .bind("cutoff", DateTime.now().minusMinutes(minutes))
                        .bindList("taps", taps)
                        .bindList("exclude_client_macs", excludeClientMacs == null || excludeClientMacs.isEmpty() ?
                                noValuesBindList : excludeClientMacs)
                        .define("order_column", orderColumn.getColumnName())
                        .define("order_direction", orderDirection)
                        .bind("limit", limit)
                        .bind("offset", offset)
                        .mapTo(DisconnectedClientDetails.class)
                        .list()
        );
    }

    public List<ClientHistogramEntry> getClientHistogram(int minutes, List<UUID> taps, List<String> excludeClientMacs) {
        if (taps.isEmpty()) {
            return Collections.emptyList();
        }

        return nzyme.getDatabase().withHandle(handle ->
                handle.createQuery("SELECT COUNT(DISTINCT(c.client_mac)) AS client_count, " +
                                "DATE_TRUNC('minute', c.created_at) as bucket " +
                                "FROM dot11_clients AS c " +
                                "WHERE c.created_at > :cutoff AND c.tap_uuid IN (<taps>) " +
                                "AND NOT c.client_mac IN (<exclude_client_macs>) " +
                                "GROUP BY bucket " +
                                "ORDER BY bucket DESC")
                        .bind("cutoff", DateTime.now().minusMinutes(minutes))
                        .bindList("taps", taps)
                        .bindList("exclude_client_macs", excludeClientMacs == null || excludeClientMacs.isEmpty() ?
                                noValuesBindList : excludeClientMacs)
                        .mapTo(ClientHistogramEntry.class)
                        .list()
        );
    }

    public List<ClientHistogramEntry> getConnectedClientHistogram(int minutes, List<UUID> taps) {
        if (taps.isEmpty()) {
            return Collections.emptyList();
        }

        return nzyme.getDatabase().withHandle(handle ->
                handle.createQuery("SELECT COUNT(DISTINCT(c.client_mac)) AS client_count, " +
                                "DATE_TRUNC('minute', b.created_at) as bucket " +
                                "FROM dot11_bssids AS b " +
                                "LEFT JOIN dot11_bssid_clients c on b.id = c.bssid_id " +
                                "WHERE b.created_at > :cutoff AND b.tap_uuid IN (<taps>) " +
                                "GROUP BY bucket " +
                                "ORDER BY bucket DESC")
                        .bind("cutoff", DateTime.now().minusMinutes(minutes))
                        .bindList("taps", taps)
                        .mapTo(ClientHistogramEntry.class)
                        .list()
        );
    }

    public List<String> findBSSIDsClientWasConnectedTo(String clientMac, List<UUID> taps) {
        if (taps.isEmpty()) {
            return Collections.emptyList();
        }

        return nzyme.getDatabase().withHandle(handle ->
                handle.createQuery("SELECT b.bssid " +
                                "FROM dot11_bssids AS b " +
                                "LEFT JOIN dot11_bssid_clients c on b.id = c.bssid_id " +
                                "WHERE c.client_mac = :client_mac " +
                                "AND b.tap_uuid IN (<taps>)" +
                                "GROUP by b.bssid")
                        .bind("client_mac", clientMac)
                        .bindList("taps", taps)
                        .mapTo(String.class)
                        .list()
        );
    }

    public List<String> findSSIDsAdvertisedByBSSID(String bssid, List<UUID> taps) {
        if (taps.isEmpty()) {
            return Collections.emptyList();
        }

        Optional<String[]> x = nzyme.getDatabase().withHandle(handle ->
                handle.createQuery("SELECT ARRAY_AGG(DISTINCT(s.ssid)) " +
                                "FROM dot11_ssids AS s " +
                                "WHERE s.created_at > (NOW() - INTERVAL '3 days') AND " +
                                "s.bssid = :bssid AND s.tap_uuid IN (<taps>)")
                        .bind("bssid", bssid)
                        .bindList("taps", taps)
                        .mapTo(String[].class)
                        .findOne()
        );

        if (x.isEmpty()) {
            return Collections.emptyList();
        }

        List<String> result = Lists.newArrayList();
        for (String s : x.get()) {
            result.add(s);
        }

        return result;
    }

    public Optional<ClientDetails> findMergedConnectedOrDisconnectedClient(String clientMac, List<UUID> taps) {
        if (taps.isEmpty()) {
            return Optional.empty();
        }

        Optional<FirstLastSeenTuple> connected = nzyme.getDatabase().withHandle(handle ->
                handle.createQuery("SELECT MAX(b.created_at) AS last_seen, MIN(b.created_at) AS first_seen " +
                                "FROM dot11_bssids AS b " +
                                "LEFT JOIN dot11_bssid_clients AS c on b.id = c.bssid_id " +
                                "WHERE c.client_mac = :client_mac AND b.tap_uuid IN (<taps>)")
                        .bind("client_mac", clientMac)
                        .bindList("taps", taps)
                        .mapTo(FirstLastSeenTuple.class)
                        .findOne()
        );

        // Aggregations might return NULL.
        if (connected.isPresent()
                && connected.get().firstSeen() == null && connected.get().lastSeen() == null) {
            connected = Optional.empty();
        }

        Optional<String> currentlyConnectedBSSID = nzyme.getDatabase().withHandle(handle ->
                handle.createQuery("SELECT b.bssid " +
                                "FROM dot11_bssids AS b " +
                                "LEFT JOIN dot11_bssid_clients AS c on b.id = c.bssid_id " +
                                "WHERE c.client_mac = :client_mac AND b.tap_uuid IN (<taps>) " +
                                "ORDER BY b.created_at DESC " +
                                "LIMIT 1")
                        .bind("client_mac", clientMac)
                        .bindList("taps", taps)
                        .mapTo(String.class)
                        .findOne()
        );

        List<ConnectedBSSID> connectedBSSIDs = Lists.newArrayList();
        List<ClientActivityHistogramEntry> connectedHistogram;
        if (connected.isPresent()) {
            // We have found this client as connected client.
            for (String bssid : findBSSIDsClientWasConnectedTo(clientMac, taps)) {
                List<String> advertisedSSIDs = findSSIDsAdvertisedByBSSID(bssid, taps);

                connectedBSSIDs.add(ConnectedBSSID.create(
                        bssid,
                        nzyme.getOUIManager().lookupMac(bssid),
                        advertisedSSIDs
                ));
            }
            connectedHistogram = nzyme.getDatabase().withHandle(handle ->
                    handle.createQuery("SELECT DATE_TRUNC('minute', b.created_at) as bucket, " +
                                    "COALESCE(SUM(c.rx_frames) + SUM(c.tx_frames), 0) AS frames " +
                                    "FROM dot11_bssids AS b " +
                                    "LEFT JOIN dot11_bssid_clients c on b.id = c.bssid_id " +
                                    "WHERE b.created_at > :cutoff AND b.tap_uuid IN (<taps>) " +
                                    "AND c.client_mac = :client_mac " +
                                    "GROUP BY bucket " +
                                    "ORDER BY bucket DESC")
                            .bind("cutoff", DateTime.now().minusMinutes(24*60))
                            .bind("client_mac", clientMac)
                            .bindList("taps", taps)
                            .mapTo(ClientActivityHistogramEntry.class)
                            .list()
            );
        } else {
            connectedBSSIDs = Collections.emptyList();
            connectedHistogram = Collections.emptyList();
        }

        Optional<FirstLastSeenTuple> disconnected = nzyme.getDatabase().withHandle(handle ->
                handle.createQuery("SELECT MAX(c.created_at) AS last_seen, MIN(c.created_at) AS first_seen " +
                                "FROM dot11_clients AS c " +
                                "WHERE c.client_mac = :client_mac AND c.tap_uuid IN (<taps>)")
                        .bind("client_mac", clientMac)
                        .bindList("taps", taps)
                        .mapTo(FirstLastSeenTuple.class)
                        .findOne()
        );

        // Aggregations might return NULL.
        if (disconnected.isPresent()
                && disconnected.get().firstSeen() == null && disconnected.get().lastSeen() == null) {
            disconnected = Optional.empty();
        }

        List<String> probeRequests;
        List<ClientActivityHistogramEntry> disconnectedHistogram;
        if (disconnected.isPresent()) {
            probeRequests = findProbeRequestsOfClient(clientMac, taps);
            disconnectedHistogram = nzyme.getDatabase().withHandle(handle ->
                    handle.createQuery("SELECT DATE_TRUNC('minute', c.created_at) as bucket, " +
                                    "COALESCE(SUM(wildcard_probe_requests), 0) " +
                                    "+ COALESCE(SUM(pr.frame_count), 0) AS frames " +
                                    "FROM dot11_clients AS c " +
                                    "LEFT JOIN dot11_client_probereq_ssids AS pr on c.id = pr.client_id " +
                                    "WHERE c.created_at > :cutoff AND c.tap_uuid IN (<taps>) " +
                                    "AND client_mac = :client_mac " +
                                    "GROUP BY bucket " +
                                    "ORDER BY bucket DESC")
                            .bind("cutoff", DateTime.now().minusMinutes(24*60))
                            .bind("client_mac", clientMac)
                            .bindList("taps", taps)
                            .mapTo(ClientActivityHistogramEntry.class)
                            .list()
            );
        } else {
            probeRequests = Collections.emptyList();
            disconnectedHistogram = Collections.emptyList();
        }

        if (connected.isEmpty() && disconnected.isEmpty()) {
            // Client not found.
            return Optional.empty();
        }

        // Decide which last seen is more recent.
        DateTime lastSeen;
        DateTime firstSeen;
        if (connected.isPresent() && disconnected.isPresent()) {
            if (connected.get().lastSeen().isAfter(disconnected.get().lastSeen())) {
                lastSeen = connected.get().lastSeen();
            } else {
                lastSeen = disconnected.get().lastSeen();
            }

            if (connected.get().firstSeen().isBefore(disconnected.get().firstSeen())) {
                firstSeen = connected.get().lastSeen();
            } else {
                firstSeen = disconnected.get().lastSeen();
            }
        } else if (connected.isPresent()) {
            lastSeen = connected.get().lastSeen();
            firstSeen = connected.get().firstSeen();
        } else {
            lastSeen = disconnected.get().lastSeen();
            firstSeen = disconnected.get().firstSeen();
        }

        return Optional.of(ClientDetails.create(
                clientMac,
                nzyme.getOUIManager().lookupMac(clientMac),
                currentlyConnectedBSSID.map(
                        s -> ConnectedBSSID.create(s, nzyme.getOUIManager().lookupMac(s), Lists.newArrayList()
                )).orElse(null),
                connectedBSSIDs,
                firstSeen,
                lastSeen,
                probeRequests,
                connectedHistogram,
                disconnectedHistogram
        ));
    }

    public void createMonitoredSSID(String ssid, UUID organizationId, UUID tenantId) {
        nzyme.getDatabase().useHandle(handle ->
            handle.createUpdate("INSERT INTO dot11_monitored_networks(uuid, ssid, organization_id, tenant_id, " +
                            "created_at, updated_at) VALUES(:uuid, :ssid, :organization_id, :tenant_id, " +
                            "NOW(), NOW())")
                    .bind("uuid", UUID.randomUUID())
                    .bind("ssid", ssid)
                    .bind("organization_id", organizationId)
                    .bind("tenant_id", tenantId)
                    .execute()
        );
    }

    public void bumpMonitoredSSIDUpdatedAt(long networkId) {
        nzyme.getDatabase().useHandle(handle -> {
            handle.createUpdate("UPDATE dot11_monitored_networks SET updated_at = NOW() WHERE id = :id")
                    .bind("id", networkId)
                    .execute();
        });
    }

    public void setMonitoredSSIDEnabledState(long networkId, boolean enabled) {
        nzyme.getDatabase().useHandle(handle -> {
            handle.createUpdate("UPDATE dot11_monitored_networks SET enabled = :enabled WHERE id = :id")
                    .bind("enabled", enabled)
                    .bind("id", networkId)
                    .execute();
        });
    }

    public void deleteMonitoredSSID(long networkId) {
        nzyme.getDatabase().useHandle(handle -> {
            handle.createUpdate("DELETE FROM dot11_monitored_networks WHERE id = :id")
                    .bind("id", networkId)
                    .execute();
        });
    }

    public Optional<MonitoredSSID> findMonitoredSSID(UUID uuid) {
        return nzyme.getDatabase().withHandle(handle ->
            handle.createQuery("SELECT * FROM dot11_monitored_networks WHERE uuid = :uuid LIMIT 1")
                    .bind("uuid", uuid)
                    .mapTo(MonitoredSSID.class)
                    .findOne()
        );
    }

    public List<MonitoredSSID> findAllMonitoredSSIDs(@Nullable UUID organizationId, @Nullable UUID tenantId) {
        return nzyme.getDatabase().withHandle(handle -> {
            Query query;
            if (organizationId == null && tenantId == null) {
                // Super Admin.
                query = handle.createQuery("SELECT * FROM dot11_monitored_networks ORDER BY ssid ASC");
            } else if (organizationId != null && tenantId == null) {
                // Organization Admin.
                query = handle.createQuery("SELECT * FROM dot11_monitored_networks " +
                                "WHERE organization_id = :organization_id ORDER BY ssid ASC")
                        .bind("organization_id", organizationId);
            } else {
                // Tenant User.
                query = handle.createQuery("SELECT * FROM dot11_monitored_networks " +
                                "WHERE organization_id = :organization_id AND tenant_id = :tenant_id ORDER BY ssid ASC")
                        .bind("organization_id", organizationId)
                        .bind("tenant_id", tenantId);
            }

            return query.mapTo(MonitoredSSID.class).list();
        });
    }

    public Optional<Long> findMonitoredBSSIDId(long networkId, UUID bssidUUID) {
        return nzyme.getDatabase().withHandle(handle ->
            handle.createQuery("SELECT b.id FROM dot11_monitored_networks AS s " +
                            "LEFT JOIN dot11_monitored_networks_bssids b on s.id = b.monitored_network_id " +
                            "WHERE s.id = :network_id AND b.uuid = :bssid_uuid LIMIT 1")
                    .bind("network_id", networkId)
                    .bind("bssid_uuid", bssidUUID)
                    .mapTo(Long.class)
                    .findOne()
        );
    }

    public List<MonitoredBSSID> findMonitoredBSSIDsOfSSID(long ssidId) {
        return nzyme.getDatabase().withHandle(handle ->
                handle.createQuery("SELECT * FROM dot11_monitored_networks_bssids " +
                                "WHERE monitored_network_id = :ssid_id ORDER BY bssid ASC")
                        .bind("ssid_id", ssidId)
                        .mapTo(MonitoredBSSID.class)
                        .list()
        );
    }

    public List<MonitoredFingerprint> findMonitoredFingerprintsOfMonitoredBSSID(long bssidId) {
        return nzyme.getDatabase().withHandle(handle ->
                handle.createQuery("SELECT * FROM dot11_monitored_networks_fingerprints " +
                                "WHERE monitored_network_bssid_id = :bssid_id ORDER BY fingerprint ASC")
                        .bind("bssid_id", bssidId)
                        .mapTo(MonitoredFingerprint.class)
                        .list()
        );
    }

    public void createMonitoredBSSID(long monitoredNetworkId, String bssid) {
        String uppercaseBSSID = bssid.toUpperCase();
        if (!Tools.isValidMacAddress(uppercaseBSSID)) {
            throw new RuntimeException("Invalid MAC address: " + uppercaseBSSID);
        }

        nzyme.getDatabase().useHandle(handle ->
                handle.createUpdate("INSERT INTO dot11_monitored_networks_bssids(uuid, monitored_network_id, " +
                                "bssid) VALUES(:uuid, :monitored_network_id, :bssid)")
                        .bind("uuid", UUID.randomUUID())
                        .bind("monitored_network_id", monitoredNetworkId)
                        .bind("bssid", uppercaseBSSID)
                        .execute()
        );
    }

    public void deleteMonitoredBSSID(long id) {
        nzyme.getDatabase().useHandle(handle ->
                handle.createUpdate("DELETE FROM dot11_monitored_networks_bssids WHERE id = :id")
                        .bind("id", id)
                        .execute()
        );
    }

    public void createdMonitoredBSSIDFingerprint(long bssidId, String fingerprint) {
        if (fingerprint == null || fingerprint.length() != 64) {
            throw new RuntimeException("Invalid fingerprint: " + fingerprint);
        }

        nzyme.getDatabase().useHandle(handle ->
                handle.createUpdate("INSERT INTO dot11_monitored_networks_fingerprints(uuid, " +
                                "monitored_network_bssid_id, fingerprint) VALUES(:uuid, :monitored_network_bssid_id, " +
                                ":fingerprint)")
                        .bind("uuid", UUID.randomUUID())
                        .bind("monitored_network_bssid_id", bssidId)
                        .bind("fingerprint", fingerprint)
                        .execute()
        );
    }

    public void deleteMonitoredBSSIDFingerprint(long bssidId, UUID fingerprintUUID) {
        nzyme.getDatabase().useHandle(handle ->
                handle.createUpdate("DELETE FROM dot11_monitored_networks_fingerprints " +
                                "WHERE monitored_network_bssid_id = :bssidId AND uuid = :uuid")
                        .bind("bssidId", bssidId)
                        .bind("uuid", fingerprintUUID)
                        .execute()
        );
    }

    public List<MonitoredChannel> findMonitoredChannelsOfMonitoredNetwork(long ssidId) {
        return nzyme.getDatabase().withHandle(handle ->
                handle.createQuery("SELECT * FROM dot11_monitored_networks_channels " +
                                "WHERE monitored_network_id = :ssid_id ORDER BY frequency ASC")
                        .bind("ssid_id", ssidId)
                        .mapTo(MonitoredChannel.class)
                        .list()
        );
    }

    public void createMonitoredChannel(long monitoredNetworkId, long frequency) {
        nzyme.getDatabase().useHandle(handle ->
                handle.createUpdate("INSERT INTO dot11_monitored_networks_channels(uuid, monitored_network_id, " +
                                "frequency) VALUES(:uuid, :monitored_network_id, :frequency)")
                        .bind("uuid", UUID.randomUUID())
                        .bind("monitored_network_id", monitoredNetworkId)
                        .bind("frequency", frequency)
                        .execute()
        );
    }

    public void deleteMonitoredChannel(long monitoredNetworkId, UUID channelUUID) {
        nzyme.getDatabase().useHandle(handle ->
                handle.createUpdate("DELETE FROM dot11_monitored_networks_channels " +
                                "WHERE monitored_network_id = :monitored_network_id AND uuid = :uuid")
                        .bind("monitored_network_id", monitoredNetworkId)
                        .bind("uuid", channelUUID)
                        .execute()
        );
    }

    public List<MonitoredSecuritySuite> findMonitoredSecuritySuitesOfMonitoredNetwork(long ssidId) {
        return nzyme.getDatabase().withHandle(handle ->
                handle.createQuery("SELECT * FROM dot11_monitored_networks_security_suites " +
                                "WHERE monitored_network_id = :ssid_id ORDER BY suite ASC")
                        .bind("ssid_id", ssidId)
                        .mapTo(MonitoredSecuritySuite.class)
                        .list()
        );
    }

    public void createMonitoredSecuritySuite(long monitoredNetworkId, String suite) {
        nzyme.getDatabase().useHandle(handle ->
                handle.createUpdate("INSERT INTO dot11_monitored_networks_security_suites(uuid, " +
                                "monitored_network_id, suite) VALUES(:uuid, :monitored_network_id, :suite)")
                        .bind("uuid", UUID.randomUUID())
                        .bind("monitored_network_id", monitoredNetworkId)
                        .bind("suite", suite)
                        .execute()
        );
    }

    public void deleteMonitoredSecuritySuite(long monitoredNetworkId, UUID suiteUUID) {
        nzyme.getDatabase().useHandle(handle ->
                handle.createUpdate("DELETE FROM dot11_monitored_networks_security_suites " +
                                "WHERE monitored_network_id = :monitored_network_id AND uuid = :uuid")
                        .bind("monitored_network_id", monitoredNetworkId)
                        .bind("uuid", suiteUUID)
                        .execute()
        );
    }

    public static String securitySuitesToIdentifier(Dot11SecuritySuiteJson suite) {
        if (suite.groupCipher() == null && suite.pairwiseCiphers() == null && suite.keyManagementModes() == null) {
            return "NONE";
        }
        return suite.groupCipher() + "-" + suite.pairwiseCiphers() + "/" + suite.keyManagementModes();
    }

    public static String securitySuitesToIdentifier(Dot11SecurityInformationReport suite) {
        if (Strings.isNullOrEmpty(suite.suites().groupCipher())
                && suite.suites().pairwiseCiphers().isEmpty()
                && suite.suites().keyManagementModes().isEmpty()) {
            return "NONE";
        }

        return suite.suites().groupCipher() + "-"
                + Joiner.on(",").join(suite.suites().pairwiseCiphers()) + "/"
                + Joiner.on(",").join(suite.suites().keyManagementModes());
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
