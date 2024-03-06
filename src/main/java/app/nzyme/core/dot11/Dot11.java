package app.nzyme.core.dot11;

import app.nzyme.core.NzymeNode;
import app.nzyme.core.context.db.MacAddressContextEntry;
import app.nzyme.core.database.OrderDirection;
import app.nzyme.core.dot11.db.*;
import app.nzyme.core.dot11.db.monitoring.*;
import app.nzyme.core.dot11.monitoring.disco.db.Dot11DiscoMonitorMethodConfiguration;
import app.nzyme.core.dot11.tracks.db.TrackDetectorConfig;
import app.nzyme.core.rest.authentication.AuthenticatedUser;
import app.nzyme.core.rest.resources.taps.reports.tables.dot11.Dot11SecurityInformationReport;
import app.nzyme.core.rest.responses.dot11.Dot11MacAddressContextResponse;
import app.nzyme.core.rest.responses.dot11.Dot11MacAddressResponse;
import app.nzyme.core.rest.responses.dot11.clients.ConnectedBSSID;
import app.nzyme.core.util.TimeRange;
import app.nzyme.core.util.Tools;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.base.Joiner;
import com.google.common.base.Strings;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import org.jdbi.v3.core.statement.Query;
import org.joda.time.DateTime;

import javax.annotation.Nullable;
import java.util.*;
import java.util.concurrent.TimeUnit;

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

    public enum MonitorActiveStatusTypeColumn {

        UNEXPECTED_BSSID("enabled_unexpected_bssid"),
        UNEXPECTED_CHANNEL("enabled_unexpected_channel"),
        UNEXPECTED_SECURITY_SUITES("enabled_unexpected_security_suites"),
        UNEXPECTED_FINGERPRINT("enabled_unexpected_fingerprint"),
        UNEXPECTED_SIGNAL_TRACKS("enabled_unexpected_signal_tracks"),
        DISCO_MONITOR("enabled_disco_monitor"),
        SIMILAR_SSIDS("enabled_similar_looking_ssid"),
        RESTRICTED_SSID_SUBSTRINGS("enabled_ssid_substring");

        private final String columnName;

        MonitorActiveStatusTypeColumn(String columnName) {
            this.columnName = columnName;
        }

        public String getColumnName() {
            return columnName;
        }

    }

    public enum DiscoType {

        DISCONNECTION(-1),
        DEAUTHENTICATION(0),
        DISASSOCIATION(1);

        private final int number;

        DiscoType(int number) {
            this.number = number;
        }

        public int getNumber() {
            return number;
        }
    }

    LoadingCache<Dot11MacAddressLookupCompositeKey, Dot11MacAddressMetadata> macAddressMetadata = CacheBuilder.newBuilder()
            .maximumSize(5000)
            .expireAfterWrite(10, TimeUnit.MINUTES)
            .build(new CacheLoader<>() {
                // TODO MOVE TO PRIVATE FETCH()
                @Override
                public Dot11MacAddressMetadata load(Dot11MacAddressLookupCompositeKey lookup){
                    return fetchMacAddressMetadataNoCache(lookup);
                }
            });

    public Dot11(NzymeNode nzyme) {
        this.nzyme = nzyme;
    }

    public Dot11MacAddressMetadata getMacAddressMetadata(String macAddress, List<UUID> taps) {
        try {
            return macAddressMetadata.get(Dot11MacAddressLookupCompositeKey.create(macAddress, taps));
        } catch(Exception e) {
            throw new RuntimeException("Could not fetch 802.11 MAC address metadata.", e);
        }
    }

    private Dot11MacAddressMetadata fetchMacAddressMetadataNoCache(Dot11MacAddressLookupCompositeKey lookup) {
        // Is this a access point?
        boolean isAccessPoint = bssidExist(lookup.mac(), Integer.MAX_VALUE, lookup.taps());

        // Is this a client?
        boolean isClient = clientExist(lookup.mac(), Integer.MAX_VALUE, lookup.taps());

        // Is it both?
        if (isAccessPoint && isClient) {
            return Dot11MacAddressMetadata.create(Dot11MacAddressType.MULTIPLE);
        } else {
            // Not both. Good.
            if (isAccessPoint) {
                return Dot11MacAddressMetadata.create(Dot11MacAddressType.ACCESS_POINT);
            } else if (isClient) {
                return Dot11MacAddressMetadata.create(Dot11MacAddressType.CLIENT);
            } else {
                // None. Shouldn't happen.
                return Dot11MacAddressMetadata.create(Dot11MacAddressType.UNKNOWN);
            }
        }
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

    public List<String> findAllRecentSSIDNames(List<UUID> taps, int minutes) {
        if (taps.isEmpty()) {
            return Collections.emptyList();
        }

        return nzyme.getDatabase().withHandle(handle ->
                handle.createQuery("SELECT DISTINCT(ssid) FROM dot11_ssids " +
                                "WHERE tap_uuid IN (<taps>) AND created_at > :cutoff " +
                                "ORDER BY ssid ASC")
                        .bindList("taps", taps)
                        .bind("cutoff", DateTime.now().minusMinutes(minutes))
                        .mapTo(String.class)
                        .list()
        );
    }

    public Optional<BSSIDSummary> findBSSID(String bssid, int minutes, List<UUID> taps) {
        return nzyme.getDatabase().withHandle(handle ->
                handle.createQuery("SELECT b.bssid, AVG(b.signal_strength_average) AS signal_strength_average, " +
                                "MIN(b.created_at) AS first_seen, MAX(b.created_at) AS last_seen, " +
                                "SUM(b.hidden_ssid_frames) as hidden_ssid_frames, " +
                                "ARRAY_AGG(DISTINCT(COALESCE(ssp.value, 'None'))) AS security_protocols, " +
                                "ARRAY_AGG(DISTINCT(f.fingerprint)) AS fingerprints, " +
                                "ARRAY_AGG(DISTINCT(s.ssid)) AS ssids, " +
                                "ARRAY_AGG(DISTINCT(i.infrastructure_type)) AS infrastructure_types, " +
                                "COUNT(DISTINCT(c.client_mac)) AS client_count " +
                                "FROM dot11_bssids AS b " +
                                "LEFT JOIN dot11_ssids AS s ON b.id = s.bssid_id " +
                                "LEFT JOIN dot11_fingerprints AS f ON b.id = f.bssid_id " +
                                "LEFT JOIN dot11_infrastructure_types AS i on s.id = i.ssid_id " +
                                "LEFT JOIN dot11_ssid_settings AS ssp on s.id = ssp.ssid_id " +
                                "AND ssp.attribute = 'security_protocol' " +
                                "LEFT JOIN dot11_bssid_clients AS c on b.id = c.bssid_id " +
                                "WHERE b.bssid = :bssid AND b.created_at > :cutoff AND b.tap_uuid IN (<taps>) " +
                                "GROUP BY b.bssid")
                        .bind("bssid", bssid)
                        .bind("cutoff", DateTime.now().minusMinutes(minutes))
                        .bindList("taps", taps)
                        .mapTo(BSSIDSummary.class)
                        .findOne()
        );
    }

    public List<Dot11AdvertisementHistogramEntry> getBSSIDAdvertisementHistogram(String bssid, int minutes, List<UUID> taps) {
        if (taps.isEmpty()) {
            return Collections.emptyList();
        }

        return nzyme.getDatabase().withHandle(handle ->
                handle.createQuery("SELECT SUM(beacon_advertisements) AS beacons, " +
                                "SUM(proberesp_advertisements) AS proberesponses, " +
                                "DATE_TRUNC('minute', created_at) AS bucket FROM dot11_ssids " +
                                "WHERE created_at > :cutoff AND tap_uuid IN (<taps>) " +
                                "AND bssid = :bssid " +
                                "GROUP BY bucket ORDER BY bucket DESC")
                        .bind("cutoff", DateTime.now().minusMinutes(minutes))
                        .bindList("taps", taps)
                        .bind("bssid", bssid)
                        .mapTo(Dot11AdvertisementHistogramEntry.class)
                        .list()
        );
    }


    public List<ActiveChannel> getBSSIDChannelUsageHistogram(String bssid, int minutes, List<UUID> taps) {
        if (taps.isEmpty()) {
            return Collections.emptyList();
        }

        return nzyme.getDatabase().withHandle(handle ->
                handle.createQuery("SELECT c.frequency, sum(c.stats_frames) AS frames, sum(c.stats_bytes) AS bytes " +
                                "FROM dot11_ssids AS s " +
                                "LEFT JOIN dot11_channels c on s.id = c.ssid_id " +
                                "WHERE created_at > :cutoff AND tap_uuid IN (<taps>) AND s.bssid = :bssid " +
                                "GROUP BY c.frequency")
                        .bind("cutoff", DateTime.now().minusMinutes(minutes))
                        .bindList("taps", taps)
                        .bind("bssid", bssid)
                        .mapTo(ActiveChannel.class)
                        .list()
        );
    }

    public List<BSSIDSummary> findBSSIDs(int minutes, List<UUID> taps) {
        if (taps.isEmpty()) {
            return Collections.emptyList();
        }

        return nzyme.getDatabase().withHandle(handle ->
                handle.createQuery("SELECT b.bssid, AVG(b.signal_strength_average) AS signal_strength_average, " +
                                "MIN(b.created_at) AS first_seen, MAX(b.created_at) AS last_seen, " +
                                "SUM(b.hidden_ssid_frames) as hidden_ssid_frames, " +
                                "ARRAY_AGG(DISTINCT(COALESCE(ssp.value, 'None'))) AS security_protocols, " +
                                "ARRAY_AGG(DISTINCT(f.fingerprint)) AS fingerprints, " +
                                "ARRAY_AGG(DISTINCT(s.ssid)) AS ssids, " +
                                "ARRAY_AGG(DISTINCT(i.infrastructure_type)) AS infrastructure_types, " +
                                "COUNT(DISTINCT(c.client_mac)) AS client_count " +
                                "FROM dot11_bssids AS b " +
                                "LEFT JOIN dot11_ssids AS s ON b.id = s.bssid_id " +
                                "LEFT JOIN dot11_fingerprints AS f ON b.id = f.bssid_id " +
                                "LEFT JOIN dot11_infrastructure_types AS i on s.id = i.ssid_id " +
                                "LEFT JOIN dot11_ssid_settings AS ssp on s.id = ssp.ssid_id " +
                                "AND ssp.attribute = 'security_protocol' " +
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
        if (taps.isEmpty()) {
            return false;
        }

        return nzyme.getDatabase().withHandle(handle ->
                handle.createQuery("SELECT EXISTS (SELECT 1 FROM dot11_bssids " +
                                "WHERE created_at > :cutoff AND tap_uuid IN (<taps>) " +
                                "AND bssid = :bssid)")
                        .bind("cutoff", DateTime.now().minusMinutes(minutes))
                        .bindList("taps", taps)
                        .bind("bssid", bssid)
                        .mapTo(Boolean.class)
                        .one()
        );
    }

    public boolean clientExist(String clientMac, int minutes, List<UUID> taps) {
        if (taps.isEmpty()) {
            return false;
        }

        return nzyme.getDatabase().withHandle(handle ->
                handle.createQuery("SELECT EXISTS (SELECT 1 FROM dot11_clients " +
                                "WHERE created_at > :cutoff AND tap_uuid IN (<taps>) " +
                                "AND client_mac = :client_mac)")
                        .bind("cutoff", DateTime.now().minusMinutes(minutes))
                        .bindList("taps", taps)
                        .bind("client_mac", clientMac)
                        .mapTo(Boolean.class)
                        .one()
        );
    }

    public List<SSIDChannelDetails> findSSIDsOfBSSID(int minutes, String bssid, List<UUID> taps) {
        if (taps.isEmpty()) {
            return Collections.emptyList();
        }

        return nzyme.getDatabase().withHandle(handle ->
                handle.createQuery("SELECT s.ssid, c.frequency, MAX(s.created_at) AS last_seen, " +
                                "ARRAY_AGG(DISTINCT(COALESCE(ssp.value, 'None'))) AS security_protocols, " +
                                "ARRAY_AGG(DISTINCT(ssw.value)) AS is_wps, " +
                                "ARRAY_AGG(DISTINCT(i.infrastructure_type)) AS infrastructure_types, " +
                                "AVG(s.signal_strength_average) AS signal_strength_average, " +
                                "SUM(c.stats_bytes) AS total_bytes, SUM(c.stats_frames) AS total_frames " +
                                "FROM dot11_ssids AS s " +
                                "LEFT JOIN dot11_channels AS c on s.id = c.ssid_id " +
                                "LEFT JOIN dot11_infrastructure_types AS i on s.id = i.ssid_id " +
                                "LEFT JOIN dot11_ssid_settings AS ssp on s.id = ssp.ssid_id " +
                                "AND ssp.attribute = 'security_protocol' " +
                                "LEFT JOIN dot11_ssid_settings AS ssw on s.id = ssw.ssid_id " +
                                "AND ssw.attribute = 'has_wps' " +
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
                                "ARRAY_AGG(DISTINCT(COALESCE(ssp.value, 'None'))) AS security_protocols, " +
                                "ARRAY_AGG(DISTINCT(sss.value)) AS security_suites, " +
                                "ARRAY_AGG(DISTINCT(ssw.value)) AS is_wps, " +
                                "ARRAY_AGG(DISTINCT(f.fingerprint)) AS fingerprints, " +
                                "ARRAY_AGG(DISTINCT(r.rate)) AS rates, " +
                                "ARRAY_AGG(DISTINCT(i.infrastructure_type)) " +
                                "AS infrastructure_types, AVG(s.signal_strength_average) AS signal_strength_average, " +
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
                                "LEFT JOIN dot11_ssid_settings AS ssp on s.id = ssp.ssid_id " +
                                "AND ssp.attribute = 'security_protocol' " +
                                "LEFT JOIN dot11_ssid_settings AS sss on s.id = sss.ssid_id " +
                                "AND sss.attribute = 'security_suite' " +
                                "LEFT JOIN dot11_ssid_settings AS ssw on s.id = ssw.ssid_id " +
                                "AND ssw.attribute = 'has_wps' " +
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

    public List<Dot11AdvertisementHistogramEntry> getSSIDAdvertisementHistogram(String bssid, String ssid, int minutes, List<UUID> taps) {
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
                        .mapTo(Dot11AdvertisementHistogramEntry.class)
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

    public List<SignalTrackHistogramEntry> getSSIDSignalStrengthWaterfall(String bssid,
                                                                          String ssid,
                                                                          int frequency,
                                                                          int minutes,
                                                                          UUID tapId) {
        return nzyme.getDatabase().withHandle(handle ->
                handle.createQuery("SELECT DATE_TRUNC('minute', s.created_at) AS bucket, signal_strength, " +
                                "SUM(frame_count) AS frame_count FROM dot11_ssids AS s " +
                                "LEFT JOIN dot11_channel_histograms h on s.id = h.ssid_id " +
                                "WHERE created_at > :cutoff AND s.tap_uuid = :tap_id AND bssid = :bssid " +
                                "AND ssid = :ssid AND h.frequency = :frequency " +
                                "GROUP BY bucket, signal_strength ORDER BY bucket DESC")
                        .bind("cutoff", DateTime.now().minusMinutes(minutes))
                        .bind("tap_id", tapId)
                        .bind("bssid", bssid)
                        .bind("ssid", ssid)
                        .bind("frequency", frequency)
                        .mapTo(SignalTrackHistogramEntry.class)
                        .list()
        );
    }

    public List<SignalTrackHistogramEntry> getBSSIDSignalStrengthWaterfall(String bssid, int minutes, UUID tapId) {
        return nzyme.getDatabase().withHandle(handle ->
                handle.createQuery("SELECT DATE_TRUNC('minute', s.created_at) AS bucket, signal_strength, " +
                                "SUM(frame_count) AS frame_count FROM dot11_ssids AS s " +
                                "LEFT JOIN dot11_channel_histograms h on s.id = h.ssid_id " +
                                "WHERE created_at > :cutoff AND s.tap_uuid = :tap_id AND bssid = :bssid " +
                                "GROUP BY bucket, signal_strength ORDER BY bucket DESC")
                        .bind("cutoff", DateTime.now().minusMinutes(minutes))
                        .bind("tap_id", tapId)
                        .bind("bssid", bssid)
                        .mapTo(SignalTrackHistogramEntry.class)
                        .list()
        );
    }

    public Optional<TrackDetectorConfig> findCustomTrackDetectorConfiguration(UUID organizationId,
                                                                              UUID tapId,
                                                                              String bssid,
                                                                              String ssid,
                                                                              int channel) {
        return nzyme.getDatabase().withHandle(handle ->
                handle.createQuery("SELECT frame_threshold, gap_threshold, signal_centerline_jitter " +
                                "FROM dot11_track_detector_configuration " +
                                "WHERE organization_id = :organization_id AND bssid = :bssid AND ssid = :ssid " +
                                "AND channel = :channel AND tap_id = :tap_id")
                        .bind("organization_id", organizationId)
                        .bind("tap_id", tapId)
                        .bind("bssid", bssid)
                        .bind("ssid", ssid)
                        .bind("channel", channel)
                        .mapTo(TrackDetectorConfig.class)
                        .findOne()
        );
    }

    public void updateCustomTrackDetectorConfiguration(UUID organizationId,
                                                       UUID tapId,
                                                       String bssid,
                                                       String ssid,
                                                       int channel,
                                                       int frameThreshold,
                                                       int gapThreshold,
                                                       int signalCenterlineJitter) {
        if (findCustomTrackDetectorConfiguration(organizationId, tapId, bssid, ssid, channel).isPresent()) {
            // Update existing configuration.
            nzyme.getDatabase().useHandle(handle ->
                    handle.createUpdate("UPDATE dot11_track_detector_configuration " +
                                    "SET frame_threshold = :frame_threshold, gap_threshold = :gap_threshold, " +
                                    "signal_centerline_jitter = :signal_centerline_jitter, updated_at = NOW() " +
                                    "WHERE organization_id = :organization_id AND bssid = :bssid AND ssid = :ssid " +
                                    "AND channel = :channel AND tap_id = :tap_id")
                            .bind("organization_id", organizationId)
                            .bind("tap_id", tapId)
                            .bind("bssid", bssid)
                            .bind("ssid", ssid)
                            .bind("channel", channel)
                            .bind("frame_threshold", frameThreshold)
                            .bind("gap_threshold", gapThreshold)
                            .bind("signal_centerline_jitter", signalCenterlineJitter)
                            .execute()
            );
        } else {
            // Write new configuration.
            nzyme.getDatabase().useHandle(handle ->
                    handle.createUpdate("INSERT INTO dot11_track_detector_configuration(uuid, organization_id, " +
                                    "tap_id, bssid, ssid, channel, frame_threshold, gap_threshold, " +
                                    "signal_centerline_jitter, updated_at, created_at) VALUES(:uuid, :organization_id, " +
                                    ":tap_id, :bssid, :ssid, :channel, :frame_threshold, :gap_threshold, " +
                                    ":signal_centerline_jitter, NOW(), NOW())")
                            .bind("uuid", UUID.randomUUID())
                            .bind("organization_id", organizationId)
                            .bind("tap_id", tapId)
                            .bind("bssid", bssid)
                            .bind("ssid", ssid)
                            .bind("channel", channel)
                            .bind("frame_threshold", frameThreshold)
                            .bind("gap_threshold", gapThreshold)
                            .bind("signal_centerline_jitter", signalCenterlineJitter)
                            .execute()
            );
        }
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

    public List<ConnectedClientDetails> findClientsOfBSSID(String bssid, int minutes, List<UUID> taps) {
        if (taps.isEmpty()) {
            return Collections.emptyList();
        }

        return nzyme.getDatabase().withHandle(handle ->
                handle.createQuery("SELECT b.bssid AS bssid, c.client_mac AS client_mac, " +
                                "MAX(b.created_at) AS last_seen " +
                                "FROM dot11_bssids AS b " +
                                "LEFT JOIN dot11_bssid_clients c on b.id = c.bssid_id " +
                                "WHERE b.bssid = :bssid AND b.created_at > :cutoff AND b.tap_uuid IN (<taps>) " +
                                "GROUP BY c.client_mac, b.bssid " +
                                "HAVING c.client_mac IS NOT NULL " +
                                "ORDER BY c.client_mac ASC ")
                        .bind("bssid", bssid)
                        .bind("cutoff", DateTime.now().minusMinutes(minutes))
                        .bindList("taps", taps)
                        .mapTo(ConnectedClientDetails.class)
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

    public Optional<ClientDetails> findMergedConnectedOrDisconnectedClient(String clientMac,
                                                                           List<UUID> taps,
                                                                           AuthenticatedUser authenticatedUser) {
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
                Optional<MacAddressContextEntry> bssidContext = nzyme.getContextService().findMacAddressContext(
                        bssid,
                        authenticatedUser.getOrganizationId(),
                        authenticatedUser.getTenantId()
                );

                List<String> advertisedSSIDs = findSSIDsAdvertisedByBSSID(bssid, taps);

                connectedBSSIDs.add(ConnectedBSSID.create(
                        Dot11MacAddressResponse.create(
                                bssid,
                                nzyme.getOUIManager().lookupMac(bssid),
                                bssidContext.map(macAddressContextEntry ->
                                                Dot11MacAddressContextResponse.create(
                                                        macAddressContextEntry.name(),
                                                        macAddressContextEntry.description()
                                                ))
                                        .orElse(null)
                        ),
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


        ConnectedBSSID currentlyConnectedBssidResponse = null;
        if (currentlyConnectedBSSID.isPresent()) {
            Optional<MacAddressContextEntry> bssidContext = nzyme.getContextService().findMacAddressContext(
                    currentlyConnectedBSSID.get(),
                    authenticatedUser.getOrganizationId(),
                    authenticatedUser.getTenantId()
            );

            currentlyConnectedBssidResponse = ConnectedBSSID.create(
                    Dot11MacAddressResponse.create(
                            currentlyConnectedBSSID.get(),
                            nzyme.getOUIManager().lookupMac(currentlyConnectedBSSID.get()),
                            bssidContext.map(macAddressContextEntry ->
                                            Dot11MacAddressContextResponse.create(
                                                    macAddressContextEntry.name(),
                                                    macAddressContextEntry.description()
                                            ))
                                    .orElse(null)
                    ), Collections.emptyList());
        }

        return Optional.of(ClientDetails.create(
                clientMac,
                nzyme.getOUIManager().lookupMac(clientMac),
                currentlyConnectedBssidResponse,
                connectedBSSIDs,
                firstSeen,
                lastSeen,
                probeRequests,
                connectedHistogram,
                disconnectedHistogram
        ));
    }

    public List<TapBasedSignalStrengthResult> findDisconnectedClientSignalStrengthPerTap(String clientMac,
                                                                                         int minutes,
                                                                                         List<UUID> taps) {
        return nzyme.getDatabase().withHandle(handle ->
                handle.createQuery("SELECT c.tap_uuid AS tap_uuid, t.name AS tap_name, " +
                                "AVG(c.signal_strength_average) AS signal_strength " +
                                "FROM dot11_clients AS c " +
                                "LEFT JOIN taps AS t ON c.tap_uuid = t.uuid " +
                                "WHERE c.client_mac = :client_mac AND c.tap_uuid IN (<taps>) AND c.created_at > :cutoff " +
                                "GROUP BY c.tap_uuid, t.name")
                        .bind("client_mac", clientMac)
                        .bindList("taps", taps)
                        .bind("cutoff", DateTime.now().minusMinutes(minutes))
                        .mapTo(TapBasedSignalStrengthResult.class)
                        .list()
        );
    }

    public List<TapBasedSignalStrengthResult> findBssidClientSignalStrengthPerTap(String clientMac,
                                                                                  int minutes,
                                                                                  List<UUID> taps) {
        return nzyme.getDatabase().withHandle(handle ->
                handle.createQuery("SELECT b.tap_uuid AS tap_uuid, t.name AS tap_name, " +
                                "AVG(c.signal_strength_average) AS signal_strength " +
                                "FROM dot11_bssid_clients AS c " +
                                "LEFT JOIN dot11_bssids AS b on b.id = c.bssid_id " +
                                "LEFT JOIN taps AS t ON b.tap_uuid = t.uuid " +
                                "WHERE c.client_mac = :client_mac AND b.tap_uuid IN (<taps>) AND b.created_at > :cutoff " +
                                "GROUP BY b.tap_uuid, t.name")
                        .bind("client_mac", clientMac)
                        .bindList("taps", taps)
                        .bind("cutoff", DateTime.now().minusMinutes(minutes))
                        .mapTo(TapBasedSignalStrengthResult.class)
                        .list()
        );
    }

    public List<ClientSignalStrengthResult> findDisconnectedClientSignalStrengthHistogram(String clientMac,
                                                                                            int minutes,
                                                                                            UUID tap) {
        return nzyme.getDatabase().withHandle(handle ->
                handle.createQuery("SELECT AVG(signal_strength_average) AS signal_strength, " +
                                "DATE_TRUNC('minute', created_at) AS bucket " +
                                "FROM dot11_clients " +
                                "WHERE client_mac = :client_mac AND tap_uuid = :tap_uuid AND created_at > :cutoff " +
                                "GROUP BY bucket ORDER BY bucket DESC")
                        .bind("client_mac", clientMac)
                        .bind("tap_uuid", tap)
                        .bind("cutoff", DateTime.now().minusMinutes(minutes))
                        .mapTo(ClientSignalStrengthResult.class)
                        .list()
        );
    }

    public List<ClientSignalStrengthResult> findBssidClientSignalStrengthHistogram(String clientMac,
                                                                                     int minutes,
                                                                                     UUID tap) {
        return nzyme.getDatabase().withHandle(handle ->
                handle.createQuery("SELECT AVG(c.signal_strength_average) AS signal_strength, " +
                                "DATE_TRUNC('minute', b.created_at) AS bucket " +
                                "FROM dot11_bssid_clients AS c " +
                                "LEFT JOIN dot11_bssids AS b ON b.id = c.bssid_id " +
                                "WHERE c.client_mac = :client_mac AND b.tap_uuid = :tap_uuid " +
                                "AND b.created_at > :cutoff " +
                                "GROUP BY bucket ORDER BY bucket DESC")
                        .bind("client_mac", clientMac)
                        .bind("tap_uuid", tap)
                        .bind("cutoff", DateTime.now().minusMinutes(minutes))
                        .mapTo(ClientSignalStrengthResult.class)
                        .list()
        );
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

    public Optional<Long> findMonitoredBSSIDId(long networkId, String bssid) {
        return nzyme.getDatabase().withHandle(handle ->
                handle.createQuery("SELECT b.id FROM dot11_monitored_networks AS s " +
                                "LEFT JOIN dot11_monitored_networks_bssids b on s.id = b.monitored_network_id " +
                                "WHERE s.id = :network_id AND b.bssid = :bssid LIMIT 1")
                        .bind("network_id", networkId)
                        .bind("bssid", bssid)
                        .mapTo(Long.class)
                        .findOne()
        );
    }

    public List<MonitoredBSSID> findMonitoredBSSIDsOfMonitoredNetwork(long ssidId) {
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

    public long createMonitoredBSSID(long monitoredNetworkId, String bssid) {
        String uppercaseBSSID = bssid.toUpperCase();
        if (!Tools.isValidMacAddress(uppercaseBSSID)) {
            throw new RuntimeException("Invalid MAC address: " + uppercaseBSSID);
        }

        return nzyme.getDatabase().withHandle(handle ->
                handle.createQuery("INSERT INTO dot11_monitored_networks_bssids(uuid, monitored_network_id, " +
                                "bssid) VALUES(:uuid, :monitored_network_id, :bssid) RETURNING *")
                        .bind("uuid", UUID.randomUUID())
                        .bind("monitored_network_id", monitoredNetworkId)
                        .bind("bssid", uppercaseBSSID)
                        .mapTo(Long.class)
                        .one()
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

    public void setMonitorAlertStatus(long monitoredNetworkId, MonitorActiveStatusTypeColumn type, boolean status) {
        nzyme.getDatabase().useHandle(handle ->
                handle.createUpdate("UPDATE dot11_monitored_networks SET <column> = :status WHERE id = :id")
                        .bind("id", monitoredNetworkId)
                        .define("column", type.getColumnName())
                        .bind("status", status)
                        .execute()
        );
    }

    public void setSimilarSSIDMonitorConfiguration(long monitoredNetworkId, int threshold) {
        nzyme.getDatabase().useHandle(handle ->
                handle.createUpdate("UPDATE dot11_monitored_networks " +
                                "SET dconf_similar_looking_ssid_threshold = :threshold " +
                                "WHERE id = :id")
                        .bind("id", monitoredNetworkId)
                        .bind("threshold", threshold)
                        .execute()
        );
    }

    public List<RestrictedSSIDSubstring> findAllRestrictedSSIDSubstrings(long monitoredNetworkId) {
        return nzyme.getDatabase().withHandle(handle ->
                handle.createQuery("SELECT * FROM dot11_monitored_networks_restricted_substrings " +
                                "WHERE monitored_network_id = :monitored_network_id ORDER BY substring")
                        .bind("monitored_network_id", monitoredNetworkId)
                        .mapTo(RestrictedSSIDSubstring.class)
                        .list()
        );
    }

    public void createRestrictedSSIDSubstring(long monitoredNetworkId, String substring) {
        nzyme.getDatabase().useHandle(handle ->
                handle.createUpdate("INSERT INTO dot11_monitored_networks_restricted_substrings(uuid, " +
                                "monitored_network_id, substring, created_at) VALUES(:uuid, " +
                                ":monitored_network_id, :substring, NOW())")
                        .bind("uuid", UUID.randomUUID())
                        .bind("monitored_network_id", monitoredNetworkId)
                        .bind("substring", substring)
                        .execute()
        );
    }

    public void deleteRestrictedSSIDSubstring(long monitoredNetworkId, UUID uuid) {
        nzyme.getDatabase().useHandle(handle ->
                handle.createUpdate("DELETE FROM dot11_monitored_networks_restricted_substrings " +
                                "WHERE monitored_network_id = :monitored_network_id AND uuid = :uuid")
                        .bind("uuid", uuid)
                        .bind("monitored_network_id", monitoredNetworkId)
                        .execute()
        );
    }

    public long countCustomBandits(UUID organizationId, UUID tenantId) {
        return nzyme.getDatabase().withHandle(handle ->
                handle.createQuery("SELECT COUNT(*) FROM dot11_bandits " +
                                "WHERE organization_id = :organization_id AND tenant_id = :tenant_id")
                        .bind("organization_id", organizationId)
                        .bind("tenant_id", tenantId)
                        .mapTo(Long.class)
                        .one()
        );
    }

    public void createCustomBandit(UUID organizationId, UUID tenantId, String name, String description) {
        nzyme.getDatabase().useHandle(handle ->
                handle.createUpdate("INSERT INTO dot11_bandits(uuid, organization_id, tenant_id, name, " +
                                "description, created_at, updated_at) VALUES(:uuid, :organization_id, :tenant_id, " +
                                ":name, :description, NOW(), NOW())")
                        .bind("organization_id", organizationId)
                        .bind("tenant_id", tenantId)
                        .bind("uuid", UUID.randomUUID())
                        .bind("name", name)
                        .bind("description", description)
                        .execute()
        );
    }

    public void editCustomBandit(long id, String name, String description) {
        nzyme.getDatabase().useHandle(handle ->
                handle.createUpdate("UPDATE dot11_bandits SET name = :name, description = :description " +
                                "WHERE id = :id")
                        .bind("id", id)
                        .bind("name", name)
                        .bind("description", description)
                        .execute()
        );
    }

    public void deleteCustomBandit(long id) {
        nzyme.getDatabase().useHandle(handle ->
                handle.createUpdate("DELETE FROM dot11_bandits WHERE id = :id")
                        .bind("id", id)
                        .execute()
        );
    }

    public List<CustomBanditDescription> findAllCustomBandits(UUID organizationId,
                                                              UUID tenantId,
                                                              int limit,
                                                              int offset) {
        return nzyme.getDatabase().withHandle(handle ->
                handle.createQuery("SELECT * FROM dot11_bandits " +
                                "WHERE organization_id = :organization_id AND tenant_id = :tenant_id " +
                                "ORDER BY name ASC LIMIT :limit OFFSET :offset")
                        .bind("organization_id", organizationId)
                        .bind("tenant_id", tenantId)
                        .bind("limit", limit)
                        .bind("offset", offset)
                        .mapTo(CustomBanditDescription.class)
                        .list()
        );
    }

    public Optional<CustomBanditDescription> findCustomBandit(UUID uuid) {
        return nzyme.getDatabase().withHandle(handle ->
                handle.createQuery("SELECT * FROM dot11_bandits WHERE uuid = :uuid")
                        .bind("uuid", uuid)
                        .mapTo(CustomBanditDescription.class)
                        .findOne()
        );
    }

    public List<String> findFingerprintsOfCustomBandit(long banditId) {
        return nzyme.getDatabase().withHandle(handle ->
                handle.createQuery("SELECT fingerprint FROM dot11_bandit_fingerprints " +
                                "WHERE bandit_id = :bandit_id")
                        .bind("bandit_id", banditId)
                        .mapTo(String.class)
                        .list()
        );
    }

    public void bumpCustomBanditUpdatedAt(long banditId) {
        nzyme.getDatabase().useHandle(handle -> {
            handle.createUpdate("UPDATE dot11_bandits SET updated_at = NOW() WHERE id = :id")
                    .bind("id", banditId)
                    .execute();
        });
    }

    public void addFingerprintOfCustomBandit(long banditId, String fingerprint) {
        if (fingerprint.length() != 64) {
            throw new RuntimeException("Fingerprint must be 64 characters long.");
        }

        nzyme.getDatabase().useHandle(handle ->
                handle.createUpdate("INSERT INTO dot11_bandit_fingerprints(bandit_id, fingerprint) " +
                                "VALUES(:bandit_id, :fingerprint)")
                        .bind("bandit_id", banditId)
                        .bind("fingerprint", fingerprint)
                        .execute()
        );
    }

    public void removeFingerprintOfCustomBandit(long banditId, String fingerprint) {
        nzyme.getDatabase().useHandle(handle ->
                handle.createUpdate("DELETE FROM dot11_bandit_fingerprints " +
                                "WHERE bandit_id = :bandit_id AND fingerprint = :fingerprint")
                        .bind("bandit_id", banditId)
                        .bind("fingerprint", fingerprint)
                        .execute()
        );
    }

    public List<DiscoHistogramEntry> getDiscoHistogram(DiscoType discoType,
                                                       int minutes,
                                                       UUID tap,
                                                       @Nullable List<String> bssids) {
        return getDiscoHistogram(discoType, minutes, List.of(tap), bssids);
    }

    public List<DiscoHistogramEntry> getDiscoHistogram(DiscoType discoType,
                                                       int minutes,
                                                       List<UUID> taps,
                                                       @Nullable List<String> bssids) {
        if (taps.isEmpty()) {
            return Collections.emptyList();
        }

        if (bssids != null && bssids.isEmpty()) {
            return Collections.emptyList();
        }

        List<Integer> discoTypes = Lists.newArrayList();
        switch (discoType) {
            case DISCONNECTION:
                discoTypes.add(DiscoType.DISASSOCIATION.getNumber());
                discoTypes.add(DiscoType.DEAUTHENTICATION.getNumber());
                break;
            case DEAUTHENTICATION:
                discoTypes.add(DiscoType.DEAUTHENTICATION.getNumber());
                break;
            case DISASSOCIATION:
                discoTypes.add(DiscoType.DISASSOCIATION.getNumber());
                break;
        }

        List<DiscoHistogramEntry> senders;
        if (bssids != null) {
            // BSSID filter applied.
            senders = nzyme.getDatabase().withHandle(handle ->
                    handle.createQuery("SELECT SUM(senders.sent_frames) " +
                                    "AS frame_count, DATE_TRUNC('minute', senders.created_at) AS bucket " +
                                    "FROM dot11_disco_activity AS senders " +
                                    "WHERE senders.disco_type IN (<disco_types>) " +
                                    "AND senders.bssid IN (<bssids>) " +
                                    "AND senders.created_at > :cutoff " +
                                    "AND senders.tap_uuid IN (<taps>) " +
                                    "GROUP BY bucket ORDER BY bucket DESC")
                            .bind("cutoff", DateTime.now().minusMinutes(minutes))
                            .bindList("disco_types", discoTypes)
                            .bindList("bssids", bssids)
                            .bindList("taps", taps)
                            .mapTo(DiscoHistogramEntry.class)
                            .list()
            );
        } else {
            // No BSSID filter.
            senders = nzyme.getDatabase().withHandle(handle ->
                    handle.createQuery("SELECT SUM(senders.sent_frames) " +
                                    "AS frame_count, DATE_TRUNC('minute', senders.created_at) AS bucket " +
                                    "FROM dot11_disco_activity AS senders " +
                                    "WHERE senders.disco_type IN (<disco_types>) " +
                                    "AND senders.created_at > :cutoff " +
                                    "AND senders.tap_uuid IN (<taps>) " +
                                    "GROUP BY bucket ORDER BY bucket DESC")
                            .bind("cutoff", DateTime.now().minusMinutes(minutes))
                            .bindList("disco_types", discoTypes)
                            .bindList("taps", taps)
                            .mapTo(DiscoHistogramEntry.class)
                            .list()
            );
        }

        List<DiscoHistogramEntry> receivers;
        if (bssids != null) {
            // BSSID filter applied.
            receivers = nzyme.getDatabase().withHandle(handle ->
                    handle.createQuery("SELECT SUM(receivers.received_frames) " +
                                    "AS frame_count, DATE_TRUNC('minute', senders.created_at) AS bucket " +
                                    "FROM dot11_disco_activity_receivers AS receivers " +
                                    "LEFT JOIN dot11_disco_activity AS senders " +
                                    "ON receivers.disco_activity_id = senders.id " +
                                    "WHERE senders.disco_type IN (<disco_types>) " +
                                    "AND receivers.bssid IN (<bssids>) " +
                                    "AND senders.created_at > :cutoff " +
                                    "AND senders.tap_uuid IN (<taps>) " +
                                    "GROUP BY bucket ORDER BY bucket DESC")
                            .bind("cutoff", DateTime.now().minusMinutes(minutes))
                            .bindList("disco_types", discoTypes)
                            .bindList("bssids", bssids)
                            .bindList("taps", taps)
                            .mapTo(DiscoHistogramEntry.class)
                            .list()
            );
        } else {
            /*
             * No BSSID filter. We don't care about receivers if there is no filter. It would lead to double-counting
             * because each sent frame shows up again as a received frame. Just taking sent_frames is enough here.
             */
            receivers = Lists.newArrayList();
        }

        Map<DateTime, DiscoHistogramEntry> sendersMap = Maps.newHashMap();
        for (DiscoHistogramEntry h : senders) {
            sendersMap.put(h.bucket(), h);
        }

        if (bssids != null && !bssids.isEmpty()) {
            if (!senders.isEmpty() && receivers.isEmpty()) {
                return senders;
            }

            if (senders.isEmpty() && !receivers.isEmpty()) {
                return receivers;
            }

            // Merge sender and receiver histograms.
            List<DiscoHistogramEntry> result = Lists.newArrayList(senders);
            for (DiscoHistogramEntry h : receivers) {
                DiscoHistogramEntry senderEntry = sendersMap.get(h.bucket());
                if (senderEntry == null) {
                    // No sender entry.
                    result.add(h);
                } else {
                    // Sender entry exists. Add receiver.
                    result.add(DiscoHistogramEntry.create(h.bucket(), h.frameCount() + senderEntry.frameCount()));
                }
            }

            return result;
        } else {
            // We only have senders.
            return senders;
        }
    }

    public List<Dot11MacFrameCount> getDiscoTopSenders(int minutes,
                                                       int limit,
                                                       int offset,
                                                       List<UUID> taps,
                                                       @Nullable List<String> bssids) {
        if (taps.isEmpty()) {
            return Collections.emptyList();
        }

        if (bssids != null && bssids.isEmpty()) {
            return Collections.emptyList();
        }

        if (bssids != null) {
            // BSSID filter applied.
            return nzyme.getDatabase().withHandle(handle ->
                    handle.createQuery("SELECT senders.bssid, SUM(senders.sent_frames) AS frame_count " +
                                    "FROM dot11_disco_activity AS senders " +
                                    "LEFT JOIN dot11_disco_activity_receivers AS receivers " +
                                    "ON senders.id = receivers.disco_activity_id " +
                                    "WHERE senders.created_at > :cutoff AND senders.tap_uuid IN (<taps>) " +
                                    "AND (senders.bssid IN (<bssids>) OR receivers.bssid IN (<bssids>)) " +
                                    "GROUP BY senders.bssid ORDER BY frame_count DESC " +
                                    "LIMIT :limit OFFSET :offset")
                            .bind("cutoff", DateTime.now().minusMinutes(minutes))
                            .bindList("taps", taps)
                            .bindList("bssids", bssids)
                            .bind("limit", limit)
                            .bind("offset", offset)
                            .mapTo(Dot11MacFrameCount.class)
                            .list()
            );
        } else {
            // No BSSID filter.
            return nzyme.getDatabase().withHandle(handle ->
                    handle.createQuery("SELECT bssid, SUM(sent_frames) AS frame_count " +
                                    "FROM dot11_disco_activity " +
                                    "WHERE created_at > :cutoff AND tap_uuid IN (<taps>) " +
                                    "GROUP BY bssid ORDER BY frame_count DESC " +
                                    "LIMIT :limit OFFSET :offset")
                            .bind("cutoff", DateTime.now().minusMinutes(minutes))
                            .bindList("taps", taps)
                            .bind("limit", limit)
                            .bind("offset", offset)
                            .mapTo(Dot11MacFrameCount.class)
                            .list()
            );
        }
    }

    public Long countDiscoTopSenders(int minutes, List<UUID> taps, @Nullable List<String> bssids) {
        if (taps.isEmpty()) {
            return 0L;
        }

        if (bssids != null && bssids.isEmpty()) {
            return 0L;
        }

        if (bssids != null) {
            // BSSID filter applied.
            return nzyme.getDatabase().withHandle(handle ->
                    handle.createQuery("SELECT COUNT(DISTINCT(senders.bssid)) " +
                                    "FROM dot11_disco_activity AS senders " +
                                    "LEFT JOIN dot11_disco_activity_receivers AS receivers " +
                                    "ON senders.id = receivers.disco_activity_id " +
                                    "WHERE senders.created_at > :cutoff AND senders.tap_uuid IN (<taps>) " +
                                    "AND (senders.bssid IN (<bssids>) OR receivers.bssid IN (<bssids>))")
                            .bind("cutoff", DateTime.now().minusMinutes(minutes))
                            .bindList("taps", taps)
                            .bindList("bssids", bssids)
                            .mapTo(Long.class)
                            .one()
            );
        } else {
            // No BSSID filter.
            return nzyme.getDatabase().withHandle(handle ->
                    handle.createQuery("SELECT COUNT(DISTINCT(bssid)) " +
                                    "FROM dot11_disco_activity " +
                                    "WHERE created_at > :cutoff AND tap_uuid IN (<taps>)")
                            .bind("cutoff", DateTime.now().minusMinutes(minutes))
                            .bindList("taps", taps)
                            .mapTo(Long.class)
                            .one()
            );
        }
    }

    public List<Dot11MacFrameCount> getDiscoTopReceivers(int minutes,
                                                         int limit,
                                                         int offset,
                                                         List<UUID> taps,
                                                         @Nullable List<String> bssids) {
        if (taps.isEmpty()) {
            return Collections.emptyList();
        }

        if (bssids != null && bssids.isEmpty()) {
            return Collections.emptyList();
        }

        if (bssids != null) {
            // BSSID filter applied.
            return nzyme.getDatabase().withHandle(handle ->
                    handle.createQuery("SELECT receivers.bssid, SUM(received_frames) AS frame_count " +
                                    "FROM dot11_disco_activity_receivers AS receivers " +
                                    "LEFT JOIN dot11_disco_activity AS senders " +
                                    "ON senders.id = receivers.disco_activity_id " +
                                    "WHERE senders.created_at > :cutoff AND senders.tap_uuid IN (<taps>) " +
                                    "AND (receivers.bssid IN (<bssids>) OR senders.bssid IN (<bssids>)) " +
                                    "GROUP BY receivers.bssid ORDER BY frame_count DESC " +
                                    "LIMIT :limit OFFSET :offset")
                            .bind("cutoff", DateTime.now().minusMinutes(minutes))
                            .bindList("taps", taps)
                            .bindList("bssids", bssids)
                            .bind("limit", limit)
                            .bind("offset", offset)
                            .mapTo(Dot11MacFrameCount.class)
                            .list()
            );
        } else {
            // No BSSID filter.
            return nzyme.getDatabase().withHandle(handle ->
                    handle.createQuery("SELECT receivers.bssid, SUM(received_frames) AS frame_count " +
                                    "FROM dot11_disco_activity_receivers AS receivers " +
                                    "LEFT JOIN dot11_disco_activity AS senders " +
                                    "ON senders.id = receivers.disco_activity_id " +
                                    "WHERE senders.created_at > :cutoff AND senders.tap_uuid IN (<taps>) " +
                                    "GROUP BY receivers.bssid ORDER BY frame_count DESC " +
                                    "LIMIT :limit OFFSET :offset")
                            .bind("cutoff", DateTime.now().minusMinutes(minutes))
                            .bindList("taps", taps)
                            .bind("limit", limit)
                            .bind("offset", offset)
                            .mapTo(Dot11MacFrameCount.class)
                            .list()
            );
        }
    }

    public Long countDiscoTopReceivers(int minutes, List<UUID> taps, @Nullable List<String> bssids) {
        if (taps.isEmpty()) {
            return 0L;
        }

        if (bssids != null && bssids.isEmpty()) {
            return 0L;
        }

        if (bssids != null) {
            // BSSID filter applied.
            return nzyme.getDatabase().withHandle(handle ->
                    handle.createQuery("SELECT COUNT(DISTINCT(receivers.bssid)) " +
                                    "FROM dot11_disco_activity_receivers AS receivers " +
                                    "LEFT JOIN dot11_disco_activity AS senders " +
                                    "ON senders.id = receivers.disco_activity_id " +
                                    "WHERE senders.created_at > :cutoff AND senders.tap_uuid IN (<taps>) " +
                                    "AND (receivers.bssid IN (<bssids>) OR senders.bssid IN (<bssids>))")
                            .bind("cutoff", DateTime.now().minusMinutes(minutes))
                            .bindList("taps", taps)
                            .bindList("bssids", bssids)
                            .mapTo(Long.class)
                            .one()
            );
        } else {
            // No BSSID filter.
            return nzyme.getDatabase().withHandle(handle ->
                    handle.createQuery("SELECT COUNT(DISTINCT(receivers.bssid)) " +
                                    "FROM dot11_disco_activity_receivers AS receivers " +
                                    "LEFT JOIN dot11_disco_activity AS senders " +
                                    "ON senders.id = receivers.disco_activity_id " +
                                    "WHERE senders.created_at > :cutoff AND senders.tap_uuid IN (<taps>)")
                            .bind("cutoff", DateTime.now().minusMinutes(minutes))
                            .bindList("taps", taps)
                            .mapTo(Long.class)
                            .one()
            );
        }
    }

    public List<BSSIDPairFrameCount> getDiscoTopPairs(int minutes,
                                                      int limit,
                                                      int offset,
                                                      List<UUID> taps,
                                                      @Nullable List<String> bssids) {
        if (taps.isEmpty()) {
            return Collections.emptyList();
        }

        if (bssids != null && bssids.isEmpty()) {
            return Collections.emptyList();
        }

        if (bssids != null) {
            // BSSID filter applied.
            return nzyme.getDatabase().withHandle(handle ->
                    handle.createQuery("SELECT senders.bssid AS sender, receivers.bssid AS receiver, " +
                                    "SUM(receivers.received_frames) AS frame_count " +
                                    "FROM dot11_disco_activity AS senders " +
                                    "LEFT JOIN dot11_disco_activity_receivers AS receivers " +
                                    "ON senders.id = receivers.disco_activity_id " +
                                    "WHERE senders.created_at > :cutoff AND senders.tap_uuid IN (<taps>) " +
                                    "AND (receivers.bssid IN (<bssids>) OR senders.bssid IN (<bssids>)) " +
                                    "GROUP BY senders.bssid, receivers.bssid " +
                                    "ORDER BY frame_count " +
                                    "DESC LIMIT :limit OFFSET :offset")
                            .bind("cutoff", DateTime.now().minusMinutes(minutes))
                            .bindList("taps", taps)
                            .bindList("bssids", bssids)
                            .bind("limit", limit)
                            .bind("offset", offset)
                            .mapTo(BSSIDPairFrameCount.class)
                            .list()
            );
        } else {
            // No BSSID filter.
            return nzyme.getDatabase().withHandle(handle ->
                    handle.createQuery("SELECT senders.bssid AS sender, receivers.bssid AS receiver, " +
                                    "SUM(receivers.received_frames) AS frame_count " +
                                    "FROM dot11_disco_activity AS senders " +
                                    "LEFT JOIN dot11_disco_activity_receivers AS receivers " +
                                    "ON senders.id = receivers.disco_activity_id " +
                                    "WHERE senders.created_at > :cutoff AND senders.tap_uuid IN (<taps>) " +
                                    "GROUP BY senders.bssid, receivers.bssid " +
                                    "ORDER BY frame_count " +
                                    "DESC LIMIT :limit OFFSET :offset")
                            .bind("cutoff", DateTime.now().minusMinutes(minutes))
                            .bindList("taps", taps)
                            .bind("limit", limit)
                            .bind("offset", offset)
                            .mapTo(BSSIDPairFrameCount.class)
                            .list()
            );
        }
    }

    public Long countDiscoTopPairs(int minutes, List<UUID> taps, @Nullable List<String> bssids) {
        if (taps.isEmpty()) {
            return 0L;
        }

        if (bssids != null && bssids.isEmpty()) {
            return 0L;
        }

        if (bssids != null) {
            // BSSID filter applied.
            return nzyme.getDatabase().withHandle(handle ->
                    handle.createQuery("SELECT COUNT(DISTINCT(senders.bssid, receivers.bssid)) " +
                                    "FROM dot11_disco_activity AS senders " +
                                    "LEFT JOIN dot11_disco_activity_receivers AS receivers " +
                                    "ON senders.id = receivers.disco_activity_id " +
                                    "WHERE senders.created_at > :cutoff AND senders.tap_uuid IN (<taps>) " +
                                    "AND (receivers.bssid IN (<bssids>) OR senders.bssid IN (<bssids>))")
                            .bind("cutoff", DateTime.now().minusMinutes(minutes))
                            .bindList("taps", taps)
                            .bindList("bssids", bssids)
                            .mapTo(Long.class)
                            .one()
            );
        } else {
            // No BSSID filter.
            return nzyme.getDatabase().withHandle(handle ->
                    handle.createQuery("SELECT COUNT(DISTINCT(senders.bssid, receivers.bssid)) " +
                                    "FROM dot11_disco_activity AS senders " +
                                    "LEFT JOIN dot11_disco_activity_receivers AS receivers " +
                                    "ON senders.id = receivers.disco_activity_id " +
                                    "WHERE senders.created_at > :cutoff AND senders.tap_uuid IN (<taps>)")
                            .bind("cutoff", DateTime.now().minusMinutes(minutes))
                            .bindList("taps", taps)
                            .mapTo(Long.class)
                            .one()
            );
        }
    }

    public Dot11DiscoMonitorMethodConfiguration getDiscoMonitorMethodConfiguration(long monitoredNetworkId) {
        Optional<MonitoredSSID> monitoredNetwork = nzyme.getDatabase().withHandle(handle ->
            handle.createQuery("SELECT * FROM dot11_monitored_networks " +
                            "WHERE id = :id")
                    .bind("id", monitoredNetworkId)
                    .mapTo(MonitoredSSID.class)
                    .findOne()
        );

        if (monitoredNetwork.isEmpty()) {
            throw new RuntimeException("Monitored network with ID <" + monitoredNetworkId + "> not found.");
        }

        ObjectMapper om = new ObjectMapper();
        Map<String, Object> configuration;

        try {
            configuration = om.readValue(monitoredNetwork.get().discoMonitorConfiguration(), new TypeReference<>() {});
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Could not parse 802.11 disconnection monitor configuration of " +
                    "monitored network [" + monitoredNetwork.get().uuid() + "].", e);
        }

        return Dot11DiscoMonitorMethodConfiguration.create(monitoredNetwork.get().discoMonitorType(), configuration);
    }

    public void setDiscoMonitorMethodConfiguration(String methodType,
                                                   String configuration,
                                                   long monitoredNetworkId) {
        nzyme.getDatabase().useHandle(handle ->
                handle.createUpdate("UPDATE dot11_monitored_networks SET disco_monitor_type = :monitor_type, " +
                                "disco_monitor_configuration = :configuration WHERE id = :id")
                        .bind("monitor_type", methodType)
                        .bind("configuration", configuration)
                        .bind("id", monitoredNetworkId)
                        .execute()
        );
    }

    public List<String> findBSSIDsAdvertisingSSID(String ssid, List<UUID> taps) {
        return nzyme.getDatabase().withHandle(handle ->
                handle.createQuery("SELECT DISTINCT(bssid) FROM dot11_ssids " +
                                "WHERE ssid = :ssid AND tap_uuid IN (<taps>) " +
                                "AND created_at >= (NOW() - INTERVAL '24 hours') " +
                                "ORDER BY bssid ASC")
                        .bind("ssid", ssid)
                        .bindList("taps", taps)
                        .mapTo(String.class)
                        .list()
        );
    }

    public List<String> findFingerprintsOfBSSID(String bssid, List<UUID> taps) {
        return nzyme.getDatabase().withHandle(handle ->
                handle.createQuery("SELECT DISTINCT(fp.fingerprint) FROM dot11_bssids AS b " +
                                "LEFT JOIN public.dot11_fingerprints fp on b.id = fp.bssid_id " +
                                "WHERE b.bssid = :bssid AND b.tap_uuid IN (<taps>) " +
                                "AND b.created_at >= (NOW() - INTERVAL '24 hours') " +
                                "ORDER BY fp.fingerprint ASC")
                        .bind("bssid", bssid)
                        .bindList("taps", taps)
                        .mapTo(String.class)
                        .list()
        );
    }

    public List<String> findSecuritySuitesOfSSID(String ssid, List<UUID> taps) {
        return nzyme.getDatabase().withHandle(handle ->
                handle.createQuery("SELECT DISTINCT(sss.value) AS security_suites " +
                                "FROM dot11_ssids AS s " +
                                "LEFT JOIN dot11_ssid_settings AS sss on s.id = sss.ssid_id " +
                                "AND sss.attribute = 'security_suite' " +
                                "WHERE s.ssid = :ssid AND s.tap_uuid IN (<taps>) " +
                                "AND s.created_at >= (NOW() - INTERVAL '24 hours') " +
                                "ORDER BY security_suites ASC")
                        .bind("ssid", ssid)
                        .bindList("taps", taps)
                        .mapTo(String.class)
                        .list()
        );
    }

    public List<Long> findChannelsOfSSID(String ssid, List<UUID> taps) {
        return nzyme.getDatabase().withHandle(handle ->
                handle.createQuery("SELECT DISTINCT(c.frequency) FROM dot11_ssids AS s " +
                                "LEFT JOIN dot11_channels AS c ON s.id = c.ssid_id " +
                                "WHERE s.ssid = :ssid AND s.tap_uuid IN (<taps>) AND c.frequency IS NOT NULL " +
                                "AND s.created_at >= (NOW() - INTERVAL '24 hours') " +
                                "ORDER BY c.frequency ASC")
                        .bind("ssid", ssid)
                        .bindList("taps", taps)
                        .mapTo(Long.class)
                        .list()
        );
    }

    public List<TapBasedSignalStrengthResult> findBSSIDSignalStrengthPerTap(String bssid, TimeRange timeRange, List<UUID> taps) {
        return nzyme.getDatabase().withHandle(handle ->
                handle.createQuery("SELECT b.tap_uuid AS tap_uuid, t.name AS tap_name, " +
                                "AVG(b.signal_strength_average) AS signal_strength " +
                                "FROM dot11_bssids AS b " +
                                "LEFT JOIN taps AS t ON b.tap_uuid = t.uuid " +
                                "WHERE b.bssid = :bssid  AND b.tap_uuid IN (<taps>) " +
                                "AND b.created_at >= :tr_from AND b.created_at <= :tr_to " +
                                "GROUP BY b.tap_uuid, t.name ORDER BY signal_strength DESC")
                        .bind("bssid", bssid)
                        .bindList("taps", taps)
                        .bind("tr_from", timeRange.from())
                        .bind("tr_to", timeRange.to())
                        .mapTo(TapBasedSignalStrengthResult.class)
                        .list()
        );
    }

    public List<TapBasedSignalStrengthResultHistogramEntry> getBSSIDSignalStrengthPerTapHistogram(String bssid,
                                                                                                  TimeRange timeRange,
                                                                                                  List<UUID> taps) {
        return nzyme.getDatabase().withHandle(handle ->
                handle.createQuery("SELECT DATE_TRUNC('minute', b.created_at) AS bucket, " +
                                "b.tap_uuid AS tap_uuid, t.name AS tap_name, " +
                                "AVG(b.signal_strength_average) AS signal_strength " +
                                "FROM dot11_bssids AS b LEFT JOIN taps AS t ON b.tap_uuid = t.uuid " +
                                "WHERE b.bssid = :bssid AND b.tap_uuid IN (<taps>) " +
                                "AND b.created_at >= :tr_from AND b.created_at <= :tr_to " +
                                "GROUP BY b.tap_uuid, t.name, bucket ORDER BY bucket DESC")
                        .bind("bssid", bssid)
                        .bindList("taps", taps)
                        .bind("tr_from", timeRange.from())
                        .bind("tr_to", timeRange.to())
                        .mapTo(TapBasedSignalStrengthResultHistogramEntry.class)
                        .list()
        );
    }

    public static String securitySuitesToIdentifier(Dot11SecuritySuiteJson suite) {
        if (Strings.isNullOrEmpty(suite.groupCipher()) && Strings.isNullOrEmpty(suite.pairwiseCiphers())
                && Strings.isNullOrEmpty(suite.keyManagementModes())) {
            return "NONE";
        }

        String id = suite.groupCipher() + "-" + suite.pairwiseCiphers() + "/" + suite.keyManagementModes();

        // Migration. We added PMF later.
        if (!Strings.isNullOrEmpty(suite.pmfMode()) && !suite.pmfMode().equals("Unavailable")) {
            id += "+PMF_" + suite.pmfMode().toUpperCase();
        } else {
            id += "+PMF_NA";
        }

        return id;
    }

    public static String securitySuitesToIdentifier(Dot11SecurityInformationReport suite) {
        if (Strings.isNullOrEmpty(suite.suites().groupCipher())
                && suite.suites().pairwiseCiphers().isEmpty()
                && suite.suites().keyManagementModes().isEmpty()) {
            return "NONE";
        }

        String id = suite.suites().groupCipher() + "-"
                + Joiner.on(",").join(suite.suites().pairwiseCiphers()) + "/"
                + Joiner.on(",").join(suite.suites().keyManagementModes());

        // Migration. We added PMF later.
        if (!Strings.isNullOrEmpty(suite.pmf()) && !suite.pmf().equals("Unavailable")) {
            id += "+PMF_" + suite.pmf().toUpperCase();
        }

        return id;
    }

    private static final Map<Integer, Integer> frequencyChannelMap = Maps.newHashMap();

    public static int frequencyToChannel(int frequency) {

        Integer c = frequencyChannelMap.get(frequency);
        if (c == null) {
            if (frequency == 2484) {
                c = 14;
            } else if (frequency == 5935) {
                /* see 802.11ax D6.1 27.3.23.2 and Annex E */
                c = 2;
            } else if (frequency < 2484) {
                c = (frequency - 2407) / 5;
            } else if (frequency >= 4910 && frequency <= 4980) {
                c = (frequency - 4000) / 5;
            } else if (frequency < 5950) {
                c = (frequency - 5000) / 5;
            } else if (frequency <= 7115) {
                c = (frequency - 5950) / 5;
            } else {
                c = -1;
            }

            // we cache the conversion, since this will called many times
            synchronized (frequencyChannelMap) {
                frequencyChannelMap.put(frequency, c);
            }
        }

        return c;
    }

}
