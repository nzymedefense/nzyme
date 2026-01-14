package app.nzyme.core.database.generic;

import org.jdbi.v3.core.mapper.RowMapper;
import org.jdbi.v3.core.statement.StatementContext;

import java.sql.ResultSet;
import java.sql.SQLException;

public class StringDoubleDoubleNumberAggregationResultMapper implements RowMapper<StringDoubleDoubleNumberAggregationResult> {
    @Override
    public StringDoubleDoubleNumberAggregationResult map(ResultSet rs, StatementContext ctx) throws SQLException {
        return StringDoubleDoubleNumberAggregationResult.create(
                rs.getString("key"), rs.getDouble("value1"), rs.getDouble("value2")
        );
    }
}
