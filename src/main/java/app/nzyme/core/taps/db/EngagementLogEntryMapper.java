package app.nzyme.core.taps.db;

import org.jdbi.v3.core.mapper.RowMapper;
import org.jdbi.v3.core.statement.StatementContext;
import org.joda.time.DateTime;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.UUID;

public class EngagementLogEntryMapper implements RowMapper<EngagementLogEntry> {

    @Override
    public EngagementLogEntry map(ResultSet rs, StatementContext ctx) throws SQLException {
        return EngagementLogEntry.create(
                rs.getLong("id"),
                rs.getString("message"),
                UUID.fromString(rs.getString("tap_uuid")),
                new DateTime(rs.getTimestamp("timestamp"))
        );
    }

}
