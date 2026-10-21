package app.nzyme.core.ethernet.beacons.db;

import org.jdbi.v3.core.mapper.RowMapper;
import org.jdbi.v3.core.statement.StatementContext;
import org.joda.time.DateTime;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.UUID;

public class BeaconSourceEntryMapper implements RowMapper<BeaconSourceEntry> {

    @Override
    public BeaconSourceEntry map(ResultSet rs, StatementContext ctx) throws SQLException {
        return BeaconSourceEntry.create(
                rs.getLong("id"),
                UUID.fromString(rs.getString("uuid")),
                rs.getLong("beacon_id"),
                rs.getString("source_address"),
                rs.getString("source_mac"),
                rs.getLong("checkin_count"),
                rs.getDouble("interval_seconds"),
                rs.getDouble("interval_jitter"),
                rs.getDouble("regularity"),
                new DateTime(rs.getTimestamp("first_seen")),
                new DateTime(rs.getTimestamp("last_seen")),
                new DateTime(rs.getTimestamp("created_at")),
                new DateTime(rs.getTimestamp("updated_at"))
        );
    }

}
