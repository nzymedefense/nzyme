package app.nzyme.core.ethernet.beacons.db;

import org.jdbi.v3.core.mapper.RowMapper;
import org.jdbi.v3.core.statement.StatementContext;
import org.joda.time.DateTime;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.UUID;

public class BeaconCheckinEntryMapper implements RowMapper<BeaconCheckinEntry> {

    @Override
    public BeaconCheckinEntry map(ResultSet rs, StatementContext ctx) throws SQLException {
        String tapUuid = rs.getString("tap_uuid");

        return BeaconCheckinEntry.create(
                rs.getLong("id"),
                rs.getLong("beacon_id"),
                rs.getLong("beacon_source_id"),
                new DateTime(rs.getTimestamp("timestamp")),
                rs.getString("destination_address"),
                rs.getString("destination_mac"),
                tapUuid == null ? null : UUID.fromString(tapUuid),
                rs.getString("session_key"),
                rs.getString("l4_type"),
                rs.getInt("session_count"),
                nullableLong(rs, "bytes_rx"),
                nullableLong(rs, "bytes_tx"),
                nullableLong(rs, "bytes_total")
        );
    }

    private static Long nullableLong(ResultSet rs, String column) throws SQLException {
        long value = rs.getLong(column);
        return rs.wasNull() ? null : value;
    }

}
