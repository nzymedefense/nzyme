package app.nzyme.core.database.generic;

import org.jdbi.v3.core.mapper.RowMapper;
import org.jdbi.v3.core.statement.StatementContext;

import java.sql.ResultSet;
import java.sql.SQLException;

public class StringNumberNumberAggregationResultMapper implements RowMapper<StringNumberNumberAggregationResult> {
    @Override
    public StringNumberNumberAggregationResult map(ResultSet rs, StatementContext ctx) throws SQLException {
        return StringNumberNumberAggregationResult.create(
                rs.getString("key"), rs.getLong("value1"), rs.getLong("value2")
        );
    }
}
