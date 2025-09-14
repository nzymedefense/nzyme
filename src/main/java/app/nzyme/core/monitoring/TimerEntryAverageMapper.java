package app.nzyme.core.monitoring;

import org.jdbi.v3.core.mapper.RowMapper;
import org.jdbi.v3.core.statement.StatementContext;

import java.sql.ResultSet;
import java.sql.SQLException;

public class TimerEntryAverageMapper implements RowMapper<TimerEntryAverage> {

    @Override
    public TimerEntryAverage map(ResultSet rs, StatementContext ctx) throws SQLException {
        return TimerEntryAverage.create(
                rs.getString("name"),
                rs.getDouble("max"),
                rs.getDouble("min"),
                rs.getDouble("mean"),
                rs.getDouble("p99"),
                rs.getDouble("stddev"),
                rs.getDouble("counter")
        );
    }

}
