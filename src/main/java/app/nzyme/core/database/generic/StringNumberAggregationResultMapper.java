package app.nzyme.core.database.generic;

import org.jdbi.v3.core.mapper.RowMapper;
import org.jdbi.v3.core.statement.StatementContext;

import java.sql.ResultSet;
import java.sql.SQLException;

public class StringNumberAggregationResultMapper implements RowMapper<StringNumberAggregationResult> {
    @Override
    public StringNumberAggregationResult map(ResultSet rs, StatementContext ctx) throws SQLException {
        return StringNumberAggregationResult.create(rs.getString("key"), rs.getLong("value"));
    }
}
