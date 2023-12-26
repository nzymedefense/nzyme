package app.nzyme.core.dot11.db.monitoring;

import org.jdbi.v3.core.mapper.RowMapper;
import org.jdbi.v3.core.statement.StatementContext;
import org.joda.time.DateTime;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.UUID;

public class MonitoredSSIDMapper implements RowMapper<MonitoredSSID> {

    @Override
    public MonitoredSSID map(ResultSet rs, StatementContext ctx) throws SQLException {
        return MonitoredSSID.create(
                rs.getLong("id"),
                UUID.fromString(rs.getString("uuid")),
                rs.getBoolean("enabled"),
                rs.getString("ssid"),
                rs.getString("organization_id") == null ? null
                        : UUID.fromString(rs.getString("organization_id")),
                rs.getString("tenant_id") == null ? null
                        : UUID.fromString(rs.getString("tenant_id")),
                rs.getBoolean("enabled_unexpected_bssid"),
                rs.getBoolean("enabled_unexpected_channel"),
                rs.getBoolean("enabled_unexpected_security_suites"),
                rs.getBoolean("enabled_unexpected_fingerprint"),
                rs.getBoolean("enabled_unexpected_signal_tracks"),
                rs.getBoolean("enabled_similar_looking_ssid"),
                rs.getBoolean("enabled_ssid_substring"),
                rs.getString("disco_monitor_type"),
                rs.getString("disco_monitor_configuration"),
                rs.getInt("dconf_similar_looking_ssid_threshold"),
                rs.getString("dconf_ssid_substring_value"),
                new DateTime(rs.getTimestamp("created_at")),
                new DateTime(rs.getTimestamp("updated_at"))
        );
    }

}
