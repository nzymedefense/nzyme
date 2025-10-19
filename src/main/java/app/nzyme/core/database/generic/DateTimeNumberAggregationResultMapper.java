package app.nzyme.core.database.generic;


import org.jdbi.v3.core.mapper.RowMapper;
import org.jdbi.v3.core.statement.StatementContext;
import org.joda.time.DateTime;

import java.sql.ResultSet;
import java.sql.SQLException;

public class DateTimeNumberAggregationResultMapper implements RowMapper<DateTimeNumberAggregationResult> {

    @Override
    public DateTimeNumberAggregationResult map(ResultSet rs, StatementContext ctx) throws SQLException {
        return DateTimeNumberAggregationResult.create(
                new DateTime(rs.getTimestamp("bucket")),
                rs.getLong("value")
        );
    }

}
