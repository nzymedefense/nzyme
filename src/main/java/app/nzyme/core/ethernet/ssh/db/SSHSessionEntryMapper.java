package app.nzyme.core.ethernet.ssh.db;

import org.jdbi.v3.core.mapper.RowMapper;
import org.jdbi.v3.core.statement.StatementContext;
import org.joda.time.DateTime;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.UUID;

public class SSHSessionEntryMapper implements RowMapper<SSHSessionEntry> {

    @Override
    public SSHSessionEntry map(ResultSet rs, StatementContext ctx) throws SQLException {
        return SSHSessionEntry.create(
                rs.getString("tcp_session_key"),
                rs.getString("client_version_version"),
                rs.getString("client_version_software"),
                rs.getString("client_version_comments"),
                rs.getString("server_version_version"),
                rs.getString("server_version_software"),
                rs.getString("server_version_comments"),
                rs.getString("connection_status"),
                rs.getInt("tunneled_bytes"),
                new DateTime(rs.getTimestamp("established_at")),
                rs.getTimestamp("terminated_at") == null ? null : new DateTime(rs.getTimestamp("terminated_at")),
                new DateTime(rs.getTimestamp("most_recent_segment_time"))
        );
    }

}
