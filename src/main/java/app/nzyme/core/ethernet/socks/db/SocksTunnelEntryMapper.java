package app.nzyme.core.ethernet.socks.db;

import org.jdbi.v3.core.mapper.RowMapper;
import org.jdbi.v3.core.statement.StatementContext;
import org.joda.time.DateTime;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.UUID;

public class SocksTunnelEntryMapper implements RowMapper<SocksTunnelEntry> {

    @Override
    public SocksTunnelEntry map(ResultSet rs, StatementContext ctx) throws SQLException {
        return SocksTunnelEntry.create(
                rs.getLong("id"),
                UUID.fromString(rs.getString("uuid")),
                UUID.fromString(rs.getString("tap_uuid")),
                rs.getString("tcp_session_key"),
                rs.getString("socks_type"),
                rs.getString("authentication_status"),
                rs.getString("handshake_status"),
                rs.getString("connection_status"),
                rs.getString("username"),
                rs.getInt("tunneled_bytes"),
                rs.getString("tunneled_destination_address"),
                rs.getString("tunneled_destination_host"),
                rs.getInt("tunneled_destination_port"),
                new DateTime(rs.getTimestamp("established_at")),
                new DateTime(rs.getTimestamp("terminated_at")),
                new DateTime(rs.getTimestamp("most_recent_segment_time")),
                new DateTime(rs.getTimestamp("updated_at")),
                new DateTime(rs.getTimestamp("created_at"))
        );
    }

}
