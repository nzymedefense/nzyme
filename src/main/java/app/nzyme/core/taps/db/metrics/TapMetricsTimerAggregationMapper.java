package app.nzyme.core.taps.db.metrics;

import org.jdbi.v3.core.mapper.RowMapper;
import org.jdbi.v3.core.statement.StatementContext;

import java.sql.ResultSet;
import java.sql.SQLException;

public class TapMetricsTimerAggregationMapper implements RowMapper<TapMetricsTimerAggregation> {
    @Override
    public TapMetricsTimerAggregation map(ResultSet rs, StatementContext ctx) throws SQLException {
        return TapMetricsTimerAggregation.create(
                rs.getString("metric_name"),
                rs.getDouble("mean"),
                rs.getDouble("p99")
        );
    }
}
