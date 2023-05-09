package app.nzyme.core.security.authentication.db;

import org.jdbi.v3.core.mapper.RowMapper;
import org.jdbi.v3.core.statement.StatementContext;
import org.joda.time.DateTime;

import java.sql.ResultSet;
import java.sql.SQLException;

public class UserEntryMapper implements RowMapper<UserEntry> {

    @Override
    public UserEntry map(ResultSet rs, StatementContext ctx) throws SQLException {
        long roleId = rs.getLong("role_id");

        DateTime lastActivity = rs.getTimestamp("last_activity") == null
                ? null : new DateTime(rs.getTimestamp("last_activity"));

        return UserEntry.create(
                rs.getLong("id"),
                rs.getLong("organization_id"),
                rs.getLong("tenant_id"),
                rs.getString("password"),
                rs.getString("password_salt"),
                roleId == 0 ? null : roleId,
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
                rs.getString("last_geo_asn")
        );
    }

}
