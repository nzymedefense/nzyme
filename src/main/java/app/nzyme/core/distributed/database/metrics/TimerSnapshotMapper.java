package app.nzyme.core.distributed.database.metrics;

import org.jdbi.v3.core.mapper.RowMapper;
import org.jdbi.v3.core.statement.StatementContext;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.UUID;

public class TimerSnapshotMapper implements RowMapper<TimerSnapshot> {

    @Override
    public TimerSnapshot map(ResultSet rs, StatementContext ctx) throws SQLException {
        return TimerSnapshot.create(
                UUID.fromString(rs.getString("node_id")),
                rs.getLong("metric_max"),
                rs.getLong("metric_min"),
                rs.getLong("metric_mean"),
                rs.getLong("metric_p99"),
                rs.getLong("metric_stddev"),
                rs.getLong("metric_counter")
        );
    }

}