package app.nzyme.core.events.db;

import org.jdbi.v3.core.mapper.RowMapper;
import org.jdbi.v3.core.statement.StatementContext;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.UUID;

public class SubscriptionEntryMapper implements RowMapper<SubscriptionEntry> {

    @Override
    public SubscriptionEntry map(ResultSet rs, StatementContext ctx) throws SQLException {
        return SubscriptionEntry.create(
                UUID.fromString(rs.getString("uuid")),
                UUID.fromString(rs.getString("action_id")),
                rs.getString("reference"),
                rs.getString("organization_id") == null
                        ? null : UUID.fromString(rs.getString("organization_id"))
        );
    }

}
