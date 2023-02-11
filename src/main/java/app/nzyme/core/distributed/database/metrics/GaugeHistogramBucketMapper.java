package app.nzyme.core.distributed.database.metrics;

import org.jdbi.v3.core.mapper.RowMapper;
import org.jdbi.v3.core.statement.StatementContext;
import org.joda.time.DateTime;

import java.sql.ResultSet;
import java.sql.SQLException;

public class GaugeHistogramBucketMapper implements RowMapper<GaugeHistogramBucket> {

    @Override
    public GaugeHistogramBucket map(ResultSet rs, StatementContext ctx) throws SQLException {
        return GaugeHistogramBucket.create(
                new DateTime(rs.getTimestamp("bucket")),
                rs.getDouble("sum"),
                rs.getDouble("average"),
                rs.getDouble("maximum"),
                rs.getDouble("minimum")
        );
    }

}