package app.nzyme.core.dot11.db.monitoring.probereq;

import org.jdbi.v3.core.mapper.RowMapper;
import org.jdbi.v3.core.statement.StatementContext;
import org.joda.time.DateTime;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.UUID;

public class MonitoredProbeRequestEntryMapper implements RowMapper<MonitoredProbeRequestEntry> {

    @Override
    public MonitoredProbeRequestEntry map(ResultSet rs, StatementContext ctx) throws SQLException {
        return MonitoredProbeRequestEntry.create(
                rs.getByte("id"),
                UUID.fromString(rs.getString("uuid")),
                UUID.fromString(rs.getString("organization_id")),
                UUID.fromString(rs.getString("tenant_id")),
                rs.getString("ssid"),
                rs.getString("notes"),
                new DateTime(rs.getTimestamp("updated_at")),
                new DateTime(rs.getTimestamp("created_at"))
        );
    }

}
