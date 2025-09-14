package app.nzyme.core.monitoring;

import org.jdbi.v3.core.mapper.RowMapper;
import org.jdbi.v3.core.statement.StatementContext;

import java.sql.ResultSet;
import java.sql.SQLException;

public class GaugeEntryAverageMapper implements RowMapper<GaugeEntryAverage> {

    @Override
    public GaugeEntryAverage map(ResultSet rs, StatementContext ctx) throws SQLException {
        return GaugeEntryAverage.create(
                rs.getString("name"),
                rs.getDouble("value")
        );
    }

}
