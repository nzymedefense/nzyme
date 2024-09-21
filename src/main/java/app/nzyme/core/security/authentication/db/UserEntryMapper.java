package app.nzyme.core.security.authentication.db;

import org.jdbi.v3.core.mapper.RowMapper;
import org.jdbi.v3.core.statement.StatementContext;
import org.joda.time.DateTime;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.UUID;

public class UserEntryMapper implements RowMapper<UserEntry> {

    @Override
    public UserEntry map(ResultSet rs, StatementContext ctx) throws SQLException {
        DateTime lastActivity = rs.getTimestamp("last_activity") == null
                ? null : new DateTime(rs.getTimestamp("last_activity"));

        String organizationId = rs.getString("organization_id");
        String tenantId = rs.getString("tenant_id");
        String defaultOrganizationId = rs.getString("default_organization");
        String defaultTenantId = rs.getString("default_tenant");

        return UserEntry.create(
                UUID.fromString(rs.getString("uuid")),
                organizationId == null ? null : UUID.fromString(organizationId),
                tenantId == null ? null : UUID.fromString(tenantId),
                rs.getString("password"),
                rs.getString("password_salt"),
                rs.getString("email"),
                rs.getString("name"),
                rs.getBoolean("is_orgadmin"),
                rs.getBoolean("is_superadmin"),
                rs.getString("totp_secret"),
                rs.getBoolean("mfa_complete"),
                rs.getString("mfa_recovery_codes"),
                new DateTime(rs.getTimestamp("updated_at")),
                new DateTime(rs.getTimestamp("created_at")),
                lastActivity,
                rs.getString("last_remote_ip"),
                rs.getString("last_geo_city"),
                rs.getString("last_geo_country"),
                rs.getString("last_geo_asn"),
                defaultOrganizationId == null ? null : UUID.fromString(defaultOrganizationId),
                defaultTenantId == null ? null : UUID.fromString(defaultTenantId),
                rs.getBoolean("access_all_tenant_taps"),
                rs.getLong("failed_login_count"),
                rs.getLong("failed_login_count") >= 5
        );
    }

}
