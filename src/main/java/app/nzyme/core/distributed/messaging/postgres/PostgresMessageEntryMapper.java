package app.nzyme.core.distributed.messaging.postgres;

import org.jdbi.v3.core.mapper.RowMapper;
import org.jdbi.v3.core.statement.StatementContext;
import org.joda.time.DateTime;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Date;
import java.util.UUID;

public class PostgresMessageEntryMapper implements RowMapper<PostgresMessageEntry> {

    @Override
    public PostgresMessageEntry map(ResultSet rs, StatementContext ctx) throws SQLException {
        long cycleLimiter = rs.getLong("cycle_limiter");
        Date acknowledgedAt = rs.getTimestamp("acknowledged_at");

        return PostgresMessageEntry.create(
                rs.getLong("id"),
                UUID.fromString(rs.getString("sender_node_id")),
                UUID.fromString(rs.getString("receiver_node_id")),
                rs.getString("type"),
                rs.getString("parameters"),
                rs.getString("status"),
                cycleLimiter == 0 ? null : cycleLimiter,
                new DateTime(rs.getTimestamp("created_at")),
                acknowledgedAt == null ? null : new DateTime(acknowledgedAt)
        );
    }

}
