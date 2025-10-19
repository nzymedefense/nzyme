package app.nzyme.core.database;

import org.jdbi.v3.core.mapper.RowMapper;
import org.jdbi.v3.core.statement.StatementContext;

import java.sql.ResultSet;
import java.sql.SQLException;

public class NumberNumberAggregationResultMapper implements RowMapper<NumberNumberAggregationResult> {
    @Override
    public NumberNumberAggregationResult map(ResultSet rs, StatementContext ctx) throws SQLException {
        return NumberNumberAggregationResult.create(rs.getLong("key"), rs.getLong("value"));
    }
}
