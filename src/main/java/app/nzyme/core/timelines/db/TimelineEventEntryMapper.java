package app.nzyme.core.timelines.db;

import org.jdbi.v3.core.mapper.RowMapper;
import org.jdbi.v3.core.statement.StatementContext;
import org.joda.time.DateTime;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.UUID;

public class TimelineEventEntryMapper implements RowMapper<TimelineEventEntry> {
    @Override
    public TimelineEventEntry map(ResultSet rs, StatementContext ctx) throws SQLException {
        return TimelineEventEntry.create(
                rs.getLong("id"),
                UUID.fromString(rs.getString("uuid")),
                UUID.fromString(rs.getString("organization_id")),
                UUID.fromString(rs.getString("tenant_id")),
                rs.getString("address"),
                rs.getString("address_type"),
                rs.getString("event_type"),
                rs.getString("event_details"),
                new DateTime(rs.getTimestamp("timestamp"))
        );
    }
}
