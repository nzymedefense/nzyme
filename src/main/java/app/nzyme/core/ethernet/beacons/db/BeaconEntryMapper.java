package app.nzyme.core.ethernet.beacons.db;

import org.jdbi.v3.core.mapper.RowMapper;
import org.jdbi.v3.core.statement.StatementContext;
import org.joda.time.DateTime;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.UUID;

public class BeaconEntryMapper implements RowMapper<BeaconEntry> {

    @Override
    public BeaconEntry map(ResultSet rs, StatementContext ctx) throws SQLException {
        int destinationPort = rs.getInt("destination_port");
        boolean destinationPortNull = rs.wasNull();

        return BeaconEntry.create(
                rs.getLong("id"),
                UUID.fromString(rs.getString("uuid")),
                UUID.fromString(rs.getString("organization_id")),
                UUID.fromString(rs.getString("tenant_id")),
                rs.getString("type"),
                rs.getString("destination_key"),
                rs.getString("destination_host"),
                rs.getString("destination_address"),
                destinationPortNull ? null : destinationPort,
                rs.getBoolean("destination_is_site_local"),
                rs.getString("name"),
                rs.getString("description"),
                rs.getString("classification"),
                rs.getTimestamp("classification_changed_at") == null
                        ? null : new DateTime(rs.getTimestamp("classification_changed_at")),
                rs.getInt("source_count"),
                rs.getLong("checkin_count"),
                rs.getDouble("interval_seconds"),
                rs.getDouble("interval_jitter"),
                rs.getString("enrichment"),
                new DateTime(rs.getTimestamp("first_seen")),
                new DateTime(rs.getTimestamp("last_seen")),
                new DateTime(rs.getTimestamp("created_at")),
                new DateTime(rs.getTimestamp("updated_at"))
        );
    }

}
