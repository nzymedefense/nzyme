package app.nzyme.core.dot11.db.monitoring;

import org.jdbi.v3.core.mapper.RowMapper;
import org.jdbi.v3.core.statement.StatementContext;
import org.joda.time.DateTime;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.UUID;

public class MonitoredSSIDMapper implements RowMapper<MonitoredSSID> {

    @Override
    public MonitoredSSID map(ResultSet rs, StatementContext ctx) throws SQLException {
        return MonitoredSSID.create(
                rs.getLong("id"),
                UUID.fromString(rs.getString("uuid")),
                rs.getString("ssid"),
                rs.getString("organization_id") == null ? null
                        : UUID.fromString(rs.getString("organization_id")),
                rs.getString("tenant_id") == null ? null
                        : UUID.fromString(rs.getString("tenant_id")),
                new DateTime(rs.getTimestamp("created_at")),
                new DateTime(rs.getTimestamp("updated_at"))
        );
    }

}
