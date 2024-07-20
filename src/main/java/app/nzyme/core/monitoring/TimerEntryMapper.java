package app.nzyme.core.monitoring;

import org.jdbi.v3.core.mapper.RowMapper;
import org.jdbi.v3.core.statement.StatementContext;
import org.joda.time.DateTime;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.UUID;

public class TimerEntryMapper implements RowMapper<TimerEntry> {

    @Override
    public TimerEntry map(ResultSet rs, StatementContext ctx) throws SQLException {
        return TimerEntry.create(
                rs.getLong("id"),
                UUID.fromString(rs.getString("node_id")),
                rs.getString("metric_name"),
                rs.getLong("metric_max"),
                rs.getLong("metric_min"),
                rs.getLong("metric_mean"),
                rs.getLong("metric_p99"),
                rs.getLong("metric_stddev"),
                rs.getLong("metric_counter"),
                new DateTime(rs.getTimestamp("created_at"))
        );
    }

}
