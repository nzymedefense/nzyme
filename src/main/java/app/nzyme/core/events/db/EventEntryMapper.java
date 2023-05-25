package app.nzyme.core.events.db;

import org.jdbi.v3.core.mapper.RowMapper;
import org.jdbi.v3.core.statement.StatementContext;
import org.joda.time.DateTime;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.UUID;

public class EventEntryMapper implements RowMapper<EventEntry> {

    @Override
    public EventEntry map(ResultSet rs, StatementContext ctx) throws SQLException {
        UUID organizationID = rs.getString("organization_id") == null ?
                null : UUID.fromString(rs.getString("organization_id"));

        UUID tenantId = rs.getString("tenant_id") == null ?
                null : UUID.fromString(rs.getString("tenant_id"));

        return EventEntry.create(
                UUID.fromString(rs.getString("uuid")),
                organizationID,
                tenantId,
                rs.getString("event_type"),
                rs.getString("reference"),
                rs.getString("actions_fired"),
                rs.getString("details"),
                new DateTime(rs.getTimestamp("created_at"))
        );
    }

}
