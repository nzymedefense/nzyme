package app.nzyme.core.dot11.db;

import org.jdbi.v3.core.mapper.RowMapper;
import org.jdbi.v3.core.statement.StatementContext;
import org.joda.time.DateTime;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.UUID;

public class ClientWithOrganizationAndTenantMapper implements RowMapper<ClientWithOrganizationAndTenant> {

    @Override
    public ClientWithOrganizationAndTenant map(ResultSet rs, StatementContext ctx) throws SQLException {
        return ClientWithOrganizationAndTenant.create(
                rs.getString("client_mac"),
                UUID.fromString(rs.getString("organization_id")),
                UUID.fromString(rs.getString("tenant_id")),
                new DateTime(rs.getTimestamp("last_seen"))
        );
    }

}
