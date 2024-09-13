package app.nzyme.core.dot11.db;

import org.jdbi.v3.core.mapper.RowMapper;
import org.jdbi.v3.core.statement.StatementContext;
import org.joda.time.DateTime;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.UUID;

public class SSIDWithOrganizationAndTenantMapper implements RowMapper<SSIDWithOrganizationAndTenant> {

    @Override
    public SSIDWithOrganizationAndTenant map(ResultSet rs, StatementContext ctx) throws SQLException {
        return SSIDWithOrganizationAndTenant.create(
                rs.getString("ssid"),
                UUID.fromString(rs.getString("organization_id")),
                UUID.fromString(rs.getString("tenant_id")),
                new DateTime(rs.getTimestamp("last_seen"))
        );
    }

}
