package app.nzyme.core.ethernet.beacons.db;

import org.jdbi.v3.core.mapper.RowMapper;
import org.jdbi.v3.core.statement.StatementContext;
import org.joda.time.DateTime;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.UUID;

public class BeaconNoteEntryMapper implements RowMapper<BeaconNoteEntry> {

    @Override
    public BeaconNoteEntry map(ResultSet rs, StatementContext ctx) throws SQLException {
        String authorUserId = rs.getString("author_user_id");

        return BeaconNoteEntry.create(
                rs.getLong("id"),
                UUID.fromString(rs.getString("uuid")),
                rs.getLong("beacon_id"),
                authorUserId == null ? null : UUID.fromString(authorUserId),
                rs.getString("author_name"),
                rs.getString("author_email"),
                rs.getString("note"),
                new DateTime(rs.getTimestamp("created_at"))
        );
    }

}
