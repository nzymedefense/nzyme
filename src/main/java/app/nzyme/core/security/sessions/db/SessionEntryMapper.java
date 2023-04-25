package app.nzyme.core.security.sessions.db;

import org.jdbi.v3.core.mapper.RowMapper;
import org.jdbi.v3.core.statement.StatementContext;
import org.joda.time.DateTime;

import java.sql.ResultSet;
import java.sql.SQLException;

public class SessionEntryMapper implements RowMapper<SessionEntry> {

    @Override
    public SessionEntry map(ResultSet rs, StatementContext ctx) throws SQLException {
        return SessionEntry.create(
                rs.getString("sessionid"),
                rs.getLong("user_id"),
                rs.getString("remote_ip"),
                new DateTime(rs.getTimestamp("created_at"))
        );
    }

}
