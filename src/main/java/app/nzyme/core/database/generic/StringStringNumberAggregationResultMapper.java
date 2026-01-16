package app.nzyme.core.database.generic;

import org.jdbi.v3.core.mapper.RowMapper;
import org.jdbi.v3.core.statement.StatementContext;

import java.sql.ResultSet;
import java.sql.SQLException;

public class StringStringNumberAggregationResultMapper implements RowMapper<StringStringNumberAggregationResult> {
    @Override
    public StringStringNumberAggregationResult map(ResultSet rs, StatementContext ctx) throws SQLException {
        return StringStringNumberAggregationResult.create(
                rs.getString("key"), rs.getString("value1"), rs.getLong("value2")
        );
    }
}
