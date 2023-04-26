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

        return SessionEntryWithUserDetails.create(
                rs.getString("sessionid"),
                rs.getLong("user_id"),
                rs.getString("remote_ip"),
                new DateTime(rs.getTimestamp("created_at")),
                lastActivity
        );
    }

}