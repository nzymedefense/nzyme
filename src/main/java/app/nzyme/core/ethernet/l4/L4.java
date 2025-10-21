package app.nzyme.core.ethernet.l4;

import app.nzyme.core.NzymeNode;
import app.nzyme.core.database.NumberNumberAggregationResult;
import app.nzyme.core.database.OrderDirection;
import app.nzyme.core.database.generic.NumberNumberNumberAggregationResult;
import app.nzyme.core.database.generic.StringNumberAggregationResult;
import app.nzyme.core.ethernet.Ethernet;
import app.nzyme.core.ethernet.l4.db.L4Numbers;
import app.nzyme.core.ethernet.l4.db.L4Session;
import app.nzyme.core.ethernet.l4.db.L4StatisticsBucket;
import app.nzyme.core.ethernet.l4.filters.L4Filters;
import app.nzyme.core.util.Bucketing;
import app.nzyme.core.util.TimeRange;
import app.nzyme.core.util.filters.FilterSql;
import app.nzyme.core.util.filters.FilterSqlFragment;
import app.nzyme.core.util.filters.Filters;

import java.util.Collections;
import java.util.List;
import java.util.UUID;

public class L4 {

    private final NzymeNode nzyme;

    public enum OrderColumn {

        SESSION_KEY("session_key"),
        STATE("state"),
        L4_TYPE("l4_type"),
        SOURCE_MAC("source_mac"),
        SOURCE_ADDRESS("source_address"),
        SOURCE_PORT("source_port"),
        DESTINATION_MAC("destination_mac"),
        DESTINATION_ADDRESS("destination_address"),
        DESTINATION_PORT("destination_port"),
        BYTES_COUNT("bytes_count"),
        START_TIME("start_time"),
        END_TIME("end_time"),
        MOST_RECENT_SEGMENT_TIME("most_recent_segment_time");

        private final String columnName;

        OrderColumn(String columnName) {
            this.columnName = columnName;
        }

        public String getColumnName() {
            return columnName;
        }

    }

    public L4(Ethernet ethernet) {
        this.nzyme = ethernet.getNzyme();
    }

    public long countAllSessions(TimeRange timeRange, Filters filters, List<UUID> taps) {
        if (taps.isEmpty()) {
            return 0;
        }

        FilterSqlFragment filterFragment = FilterSql.generate(filters, new L4Filters());

        return nzyme.getDatabase().withHandle(handle ->
                handle.createQuery("SELECT COUNT(*) FROM (SELECT session_key, l4_type, state FROM l4_sessions " +
                                "WHERE most_recent_segment_time >= :tr_from AND most_recent_segment_time <= :tr_to " +
                                "AND tap_uuid IN (<taps>) " + filterFragment.whereSql() + " " +
                                "GROUP BY session_key, l4_type, state " +
                                "HAVING 1=1 " + filterFragment.havingSql() + ")")
                        .bindList("taps", taps)
                        .bindMap(filterFragment.bindings())
                        .bind("tr_from", timeRange.from())
                        .bind("tr_to", timeRange.to())
                        .mapTo(Long.class)
                        .one()
        );
    }

    public List<L4Session> findAllSessions(TimeRange timeRange,
                                           Filters filters,
                                           int limit,
                                           int offset,
                                           OrderColumn orderColumn,
                                           OrderDirection orderDirection,
                                           List<UUID> taps) {
        if (taps.isEmpty()) {
            return Collections.emptyList();
        }

        FilterSqlFragment filterFragment = FilterSql.generate(filters, new L4Filters());

        return nzyme.getDatabase().withHandle(handle ->
                handle.createQuery("SELECT session_key, l4_type, state, ANY_VALUE(source_mac) AS source_mac, " +
                                "ANY_VALUE(source_address) AS source_address, ANY_VALUE(source_port) AS source_port, " +
                                "ANY_VALUE(source_address_geo_asn_number) AS source_address_geo_asn_number, " +
                                "ANY_VALUE(source_address_geo_asn_name) AS source_address_geo_asn_name, " +
                                "ANY_VALUE(source_address_geo_asn_domain) AS source_address_geo_asn_domain, " +
                                "ANY_VALUE(source_address_geo_city) AS source_address_geo_city, " +
                                "ANY_VALUE(source_address_geo_country_code) AS source_address_geo_country_code, " +
                                "ANY_VALUE(source_address_geo_latitude) AS source_address_geo_latitude, " +
                                "ANY_VALUE(source_address_geo_longitude) AS source_address_geo_longitude, " +
                                "ANY_VALUE(source_address_is_site_local) AS source_address_is_site_local, " +
                                "ANY_VALUE(source_address_is_loopback) AS source_address_is_loopback, " +
                                "ANY_VALUE(source_address_is_multicast) AS source_address_is_multicast, " +
                                "ANY_VALUE(destination_mac) AS destination_mac, " +
                                "ANY_VALUE(destination_address) AS destination_address, " +
                                "ANY_VALUE(destination_port) AS destination_port, " +
                                "ANY_VALUE(destination_address_geo_asn_number) AS destination_address_geo_asn_number, " +
                                "ANY_VALUE(destination_address_geo_asn_name) AS destination_address_geo_asn_name, " +
                                "ANY_VALUE(destination_address_geo_asn_domain) AS destination_address_geo_asn_domain, " +
                                "ANY_VALUE(destination_address_geo_city) AS destination_address_geo_city, " +
                                "ANY_VALUE(destination_address_geo_country_code) AS destination_address_geo_country_code, " +
                                "ANY_VALUE(destination_address_geo_latitude) AS destination_address_geo_latitude, " +
                                "ANY_VALUE(destination_address_geo_longitude) AS destination_address_geo_longitude, " +
                                "ANY_VALUE(destination_address_is_site_local) AS destination_address_is_site_local," +
                                " ANY_VALUE(destination_address_is_loopback) AS destination_address_is_loopback, " +
                                "ANY_VALUE(destination_address_is_multicast) AS destination_address_is_multicast, " +
                                "MAX(bytes_count) AS bytes_count, MAX(segments_count) AS segments_count, " +
                                "MIN(start_time) AS start_time, MAX(end_time) AS end_time, " +
                                "MAX(most_recent_segment_time) AS most_recent_segment_time, " +
                                "MIN(created_at) AS created_at " +
                                "FROM l4_sessions " +
                                "WHERE most_recent_segment_time >= :tr_from AND most_recent_segment_time <= :tr_to " +
                                "AND tap_uuid IN (<taps>) " + filterFragment.whereSql() + " " +
                                "GROUP BY session_key, l4_type, state " +
                                "HAVING 1=1 " + filterFragment.havingSql() + " " +
                                "ORDER BY <order_column> <order_direction> " +
                                "LIMIT :limit OFFSET :offset")
                        .bindList("taps", taps)
                        .bindMap(filterFragment.bindings())
                        .bind("tr_from", timeRange.from())
                        .bind("tr_to", timeRange.to())
                        .bind("limit", limit)
                        .bind("offset", offset)
                        .define("order_column", orderColumn.getColumnName())
                        .define("order_direction", orderDirection)
                        .mapTo(L4Session.class)
                        .list()
        );
    }

    public List<L4StatisticsBucket> getStatistics(TimeRange timeRange,
                                                  Bucketing.BucketingConfiguration bucketing,
                                                  List<UUID> taps) {
        if (taps.isEmpty()) {
            return Collections.emptyList();
        }

        return nzyme.getDatabase().withHandle(handle ->
                handle.createQuery("SELECT date_trunc(:date_trunc, timestamp) AS bucket, " +
                                "SUM(bytes_tcp) AS bytes_tcp, SUM(bytes_udp) AS bytes_udp, " +
                                "SUM(bytes_internal_tcp) AS bytes_internal_tcp, " +
                                "SUM(bytes_internal_udp) AS bytes_internal_udp, " +
                                "SUM(segments_tcp) AS segments_tcp, SUM(datagrams_udp) AS datagrams_udp, " +
                                "MAX(sessions_tcp) AS sessions_tcp, MAX(sessions_udp) AS sessions_udp, " +
                                "MAX(sessions_internal_tcp) AS sessions_internal_tcp, " +
                                "MAX(sessions_internal_udp) AS sessions_internal_udp " +
                                "FROM l4_statistics WHERE timestamp >= :tr_from AND timestamp <= :tr_to " +
                                "AND tap_uuid IN (<taps>) GROUP BY bucket " +
                                "ORDER BY bucket DESC")
                        .bind("date_trunc", bucketing.type().getDateTruncName())
                        .bind("tr_from", timeRange.from())
                        .bind("tr_to", timeRange.to())
                        .bindList("taps", taps)
                        .mapTo(L4StatisticsBucket.class)
                        .list()
        );
    }

    public L4Numbers getTotals(TimeRange timeRange, List<UUID> taps) {
        if (taps.isEmpty()) {
            return L4Numbers.create(0, 0, 0, 0, 0, 0);
        }

        return nzyme.getDatabase().withHandle(handle ->
                handle.createQuery("SELECT NOW() AS bucket, " +
                                "SUM(bytes_tcp) AS bytes_tcp, SUM(bytes_udp) AS bytes_udp, " +
                                "SUM(bytes_internal_tcp) AS bytes_internal_tcp, " +
                                "SUM(bytes_internal_udp) AS bytes_internal_udp, " +
                                "SUM(segments_tcp) AS segments_tcp, SUM(datagrams_udp) AS datagrams_udp " +
                                "FROM l4_statistics WHERE timestamp >= :tr_from AND timestamp <= :tr_to " +
                                "AND tap_uuid IN (<taps>)")
                        .bind("tr_from", timeRange.from())
                        .bind("tr_to", timeRange.to())
                        .bindList("taps", taps)
                        .mapTo(L4Numbers.class)
                        .one()
        );
    }


    public long countDestinationPorts(TimeRange timeRange, Filters filters, List<UUID> taps) {
        if (taps.isEmpty()) {
            return 0;
        }

        FilterSqlFragment filterFragment = FilterSql.generate(filters, new L4Filters());

        return nzyme.getDatabase().withHandle(handle ->
                handle.createQuery("SELECT COUNT(*) FROM (SELECT destination_port AS key, COUNT(*) AS value " +
                                "FROM l4_sessions WHERE most_recent_segment_time >= :tr_from " +
                                "AND most_recent_segment_time <= :tr_to " +
                                "AND tap_uuid IN (<taps>) " + filterFragment.whereSql() + " " +
                                "GROUP BY destination_port HAVING 1=1 " + filterFragment.havingSql() + ")")
                        .bind("tr_from", timeRange.from())
                        .bind("tr_to", timeRange.to())
                        .bindMap(filterFragment.bindings())
                        .bindList("taps", taps)
                        .mapTo(Long.class)
                        .one()
        );
    }

    public long countTopTrafficSources(TimeRange timeRange,
                                       Filters filters,
                                       List<UUID> taps) {
        if (taps.isEmpty()) {
            return
                    0;
        }

        FilterSqlFragment filterFragment = FilterSql.generate(filters, new L4Filters());

        return nzyme.getDatabase().withHandle(handle ->
                handle.createQuery("SELECT COUNT(*) FROM (SELECT source_mac AS key, SUM(bytes_count) AS value " +
                                "FROM l4_sessions WHERE most_recent_segment_time >= :tr_from " +
                                "AND most_recent_segment_time <= :tr_to " +
                                "AND tap_uuid IN (<taps>) " + filterFragment.whereSql() + " " +
                                "GROUP BY source_mac HAVING 1=1 " + filterFragment.havingSql() + ")")
                        .bind("tr_from", timeRange.from())
                        .bind("tr_to", timeRange.to())
                        .bindMap(filterFragment.bindings())
                        .bindList("taps", taps)
                        .mapTo(Long.class)
                        .one()
        );
    }

    public List<StringNumberAggregationResult> getTopTrafficSources(TimeRange timeRange,
                                                                    Filters filters,
                                                                    int limit,
                                                                    int offset,
                                                                    List<UUID> taps) {
        if (taps.isEmpty()) {
            return Collections.emptyList();
        }

        FilterSqlFragment filterFragment = FilterSql.generate(filters, new L4Filters());

        return nzyme.getDatabase().withHandle(handle ->
                handle.createQuery("SELECT source_mac AS key, SUM(bytes_count) AS value " +
                                "FROM l4_sessions WHERE most_recent_segment_time >= :tr_from " +
                                "AND most_recent_segment_time <= :tr_to " +
                                "AND tap_uuid IN (<taps>) " +
                                "AND source_mac IS NOT NULL " + filterFragment.whereSql() + " " +
                                "GROUP BY source_mac HAVING 1=1 " + filterFragment.havingSql() + " " +
                                "ORDER BY value DESC LIMIT :limit OFFSET :offset")
                        .bind("tr_from", timeRange.from())
                        .bind("tr_to", timeRange.to())
                        .bind("limit", limit)
                        .bind("offset", offset)
                        .bindMap(filterFragment.bindings())
                        .bindList("taps", taps)
                        .mapTo(StringNumberAggregationResult.class)
                        .list()
        );
    }

    public List<NumberNumberNumberAggregationResult> getLeastCommonNonEphemeralDestinationPorts(TimeRange timeRange,
                                                                                                Filters filters,
                                                                                                int limit,
                                                                                                int offset,
                                                                                                List<UUID> taps) {
        if (taps.isEmpty()) {
            return Collections.emptyList();
        }

        FilterSqlFragment filterFragment = FilterSql.generate(filters, new L4Filters());

        return nzyme.getDatabase().withHandle(handle ->
                handle.createQuery("SELECT destination_port AS key, COUNT(*) AS value1, " +
                                "SUM(bytes_count) AS value2 " +
                                "FROM l4_sessions WHERE destination_port < 32768 " +
                                "AND most_recent_segment_time >= :tr_from " +
                                "AND most_recent_segment_time <= :tr_to " +
                                "AND tap_uuid IN (<taps>) " + filterFragment.whereSql() + " " +
                                "GROUP BY destination_port HAVING 1=1 " + filterFragment.havingSql() + " " +
                                "ORDER BY value1 ASC LIMIT :limit OFFSET :offset")
                        .bind("tr_from", timeRange.from())
                        .bind("tr_to", timeRange.to())
                        .bind("limit", limit)
                        .bind("offset", offset)
                        .bindMap(filterFragment.bindings())
                        .bindList("taps", taps)
                        .mapTo(NumberNumberNumberAggregationResult.class)
                        .list()
        );
    }

}
