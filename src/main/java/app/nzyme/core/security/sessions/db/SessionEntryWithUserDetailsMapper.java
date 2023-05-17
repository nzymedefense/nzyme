package app.nzyme.core.security.sessions.db;

import org.jdbi.v3.core.mapper.RowMapper;
import org.jdbi.v3.core.statement.StatementContext;
import org.joda.time.DateTime;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.UUID;

public class SessionEntryWithUserDetailsMapper implements RowMapper<SessionEntryWithUserDetails> {

    @Override
    public SessionEntryWithUserDetails map(ResultSet rs, StatementContext ctx) throws SQLException {
        DateTime lastActivity = rs.getTimestamp("last_activity") == null
                ? null : new DateTime(rs.getTimestamp("last_activity"));

        DateTime mfaRequestedAt = rs.getTimestamp("mfa_requested_at") == null
                ? null : new DateTime(rs.getTimestamp("mfa_requested_at"));

        String organizationId = rs.getString("organization_id");
        String tenantId = rs.getString("tenant_id");

        return SessionEntryWithUserDetails.create(
                rs.getLong("id"),
                rs.getString("sessionid"),
                UUID.fromString(rs.getString("user_id")),
                rs.getBoolean("is_superadmin"),
                rs.getBoolean("is_orgadmin"),
                rs.getString("email"),
                rs.getString("name"),
                rs.getString("remote_ip"),
                new DateTime(rs.getTimestamp("created_at")),
                organizationId == null ? null : UUID.fromString(organizationId),
                tenantId == null ? null : UUID.fromString(tenantId),
                lastActivity,
                rs.getBoolean("mfa_valid"),
                mfaRequestedAt
        );
    }

}