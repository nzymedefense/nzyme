package app.nzyme.core.security.sessions.db;

import org.jdbi.v3.core.mapper.RowMapper;
import org.jdbi.v3.core.statement.StatementContext;
import org.joda.time.DateTime;

import java.sql.ResultSet;
import java.sql.SQLException;

public class SessionEntryWithUserDetailsMapper implements RowMapper<SessionEntryWithUserDetails> {

    @Override
    public SessionEntryWithUserDetails map(ResultSet rs, StatementContext ctx) throws SQLException {
        DateTime lastActivity = rs.getTimestamp("last_activity") == null
                ? null : new DateTime(rs.getTimestamp("last_activity"));

        DateTime mfaRequestedAt = rs.getTimestamp("mfa_requested_at") == null
                ? null : new DateTime(rs.getTimestamp("mfa_requested_at"));

        return SessionEntryWithUserDetails.create(
                rs.getLong("id"),
                rs.getString("sessionid"),
                rs.getLong("user_id"),
                rs.getBoolean("is_superadmin"),
                rs.getBoolean("is_orgadmin"),
                rs.getString("email"),
                rs.getString("name"),
                rs.getString("remote_ip"),
                new DateTime(rs.getTimestamp("created_at")),
                rs.getLong("organization_id") == 0 ? null : rs.getLong("organization_id"),
                rs.getLong("tenant_id") == 0 ? null : rs.getLong("tenant_id"),
                lastActivity,
                mfaRequestedAt
        );
    }

}