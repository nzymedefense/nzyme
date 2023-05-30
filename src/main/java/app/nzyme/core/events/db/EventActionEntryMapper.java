package app.nzyme.core.events.db;

import org.jdbi.v3.core.mapper.RowMapper;
import org.jdbi.v3.core.statement.StatementContext;
import org.joda.time.DateTime;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.UUID;

public class EventActionEntryMapper implements RowMapper<EventActionEntry> {

    @Override
    public EventActionEntry map(ResultSet rs, StatementContext ctx) throws SQLException {
        return EventActionEntry.create(
                UUID.fromString(rs.getString("uuid")),
                rs.getString("action_type"),
                UUID.fromString(rs.getString("organization_id")),
                rs.getString("name"),
                rs.getString("description"),
                rs.getString("configuration"),
                new DateTime(rs.getTimestamp("created_at")),
                new DateTime(rs.getTimestamp("updated_at"))
        );
    }

}
