package app.nzyme.core.dot11.db.monitoring;

import org.jdbi.v3.core.mapper.RowMapper;
import org.jdbi.v3.core.statement.StatementContext;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.UUID;

public class MonitoredFingerprintMapper implements RowMapper<MonitoredFingerprint> {

    @Override
    public MonitoredFingerprint map(ResultSet rs, StatementContext ctx) throws SQLException {
        return MonitoredFingerprint.create(
                rs.getLong("monitored_network_bssid_id"),
                UUID.fromString(rs.getString("uuid")),
                rs.getString("fingerprint")
        );
    }

}
