package app.nzyme.core.dot11.db.monitoring;

import org.jdbi.v3.core.mapper.RowMapper;
import org.jdbi.v3.core.statement.StatementContext;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.UUID;

public class MonitoredBSSIDMapper implements RowMapper<MonitoredBSSID> {

    @Override
    public MonitoredBSSID map(ResultSet rs, StatementContext ctx) throws SQLException {
        return MonitoredBSSID.create(
                rs.getLong("id"),
                rs.getLong("monitored_network_id"),
                UUID.fromString(rs.getString("uuid")),
                rs.getString("bssid")
        );
    }

}
