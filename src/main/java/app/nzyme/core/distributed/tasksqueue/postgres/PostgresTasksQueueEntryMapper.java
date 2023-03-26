package app.nzyme.core.distributed.tasksqueue.postgres;

import org.jdbi.v3.core.mapper.RowMapper;
import org.jdbi.v3.core.statement.StatementContext;
import org.joda.time.DateTime;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Date;
import java.util.UUID;

public class PostgresTasksQueueEntryMapper implements RowMapper<PostgresTasksQueueEntry> {

    @Override
    public PostgresTasksQueueEntry map(ResultSet rs, StatementContext ctx) throws SQLException {
        int processingTimeMs = rs.getInt("processing_time_ms");
        Date firstProcessedAt = rs.getTimestamp("first_processed_at");
        Date lastProcessedAt = rs.getTimestamp("last_processed_at");

        return PostgresTasksQueueEntry.create(
                rs.getLong("id"),
                UUID.fromString(rs.getString("sender_node_id")),
                rs.getString("type"),
                rs.getBoolean("allow_retry"),
                rs.getString("parameters"),
                new DateTime(rs.getTimestamp("created_at")),
                rs.getString("status"),
                rs.getString("previous_status"),
                rs.getInt("retries"),
                processingTimeMs == 0 ? null : processingTimeMs,
                firstProcessedAt == null ? null : new DateTime(firstProcessedAt),
                lastProcessedAt == null ? null : new DateTime(lastProcessedAt),
                rs.getBoolean("allow_process_self")
        );
    }

}
