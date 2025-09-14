package app.nzyme.core.taps.db.metrics;

import org.jdbi.v3.core.mapper.RowMapper;
import org.jdbi.v3.core.statement.StatementContext;

import java.sql.ResultSet;
import java.sql.SQLException;

public class TapMetricsGaugeAggregationMapper implements RowMapper<TapMetricsGaugeAggregation> {
    @Override
    public TapMetricsGaugeAggregation map(ResultSet rs, StatementContext ctx) throws SQLException {
        return TapMetricsGaugeAggregation.create(
                rs.getString("metric_name"),
                rs.getDouble("value")
        );
    }
}
