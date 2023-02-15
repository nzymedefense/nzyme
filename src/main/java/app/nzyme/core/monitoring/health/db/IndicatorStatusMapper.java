package app.nzyme.core.monitoring.health.db;

import org.jdbi.v3.core.mapper.RowMapper;
import org.jdbi.v3.core.statement.StatementContext;
import org.joda.time.DateTime;

import java.sql.ResultSet;
import java.sql.SQLException;

public class IndicatorStatusMapper implements RowMapper<IndicatorStatus> {

    @Override
    public IndicatorStatus map(ResultSet rs, StatementContext ctx) throws SQLException {
        return IndicatorStatus.create(
                rs.getString("indicator_name"),
                rs.getString("indicator_id"),
                new DateTime(rs.getTimestamp("last_checked")),
                rs.getString("level"),
                rs.getBoolean("active")
        );
    }

}
