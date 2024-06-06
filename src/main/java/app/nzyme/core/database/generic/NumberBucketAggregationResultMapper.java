package app.nzyme.core.database.generic;


import org.jdbi.v3.core.mapper.RowMapper;
import org.jdbi.v3.core.statement.StatementContext;
import org.joda.time.DateTime;

import java.sql.ResultSet;
import java.sql.SQLException;

public class NumberBucketAggregationResultMapper implements RowMapper<NumberBucketAggregationResult> {

    @Override
    public NumberBucketAggregationResult map(ResultSet rs, StatementContext ctx) throws SQLException {
        return NumberBucketAggregationResult.create(
                new DateTime(rs.getTimestamp("bucket")),
                rs.getLong("value")
        );
    }

}
