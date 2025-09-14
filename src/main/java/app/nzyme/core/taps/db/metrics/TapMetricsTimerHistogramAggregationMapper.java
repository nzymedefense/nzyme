package app.nzyme.core.taps.db.metrics;

import org.jdbi.v3.core.mapper.RowMapper;
import org.jdbi.v3.core.statement.StatementContext;
import org.joda.time.DateTime;

import java.sql.ResultSet;
import java.sql.SQLException;

public class TapMetricsTimerHistogramAggregationMapper implements RowMapper<TapMetricsTimer> {

    @Override
    public TapMetricsTimer map(ResultSet rs, StatementContext ctx) throws SQLException {
        return TapMetricsTimer.create(
                rs.getString("metric_name"),
                rs.getDouble("mean"),
                rs.getDouble("p99"),
                new DateTime(rs.getTimestamp("created_at"))
        );
    }

}
