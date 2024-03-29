package app.nzyme.core.security.sessions.db;

import org.jdbi.v3.core.mapper.RowMapper;
import org.jdbi.v3.core.statement.StatementContext;
import org.joda.time.DateTime;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.UUID;

public class SessionEntryMapper implements RowMapper<SessionEntry> {

    @Override
    public SessionEntry map(ResultSet rs, StatementContext ctx) throws SQLException {
        DateTime elevatedSince = rs.getTimestamp("elevated_since") == null
                ? null : new DateTime(rs.getTimestamp("elevated_since"));
        DateTime mfaRequestedAt = rs.getTimestamp("mfa_requested_at") == null
                ? null : new DateTime(rs.getTimestamp("mfa_requested_at"));

        return SessionEntry.create(
                rs.getString("sessionid"),
                UUID.fromString(rs.getString("user_id")),
                rs.getString("remote_ip"),
                rs.getBoolean("elevated"),
                elevatedSince,
                rs.getBoolean("mfa_valid"),
                new DateTime(rs.getTimestamp("created_at")),
                mfaRequestedAt
        );
    }

}
