package app.nzyme.core.ethernet.tcp.db;

import app.nzyme.core.ethernet.L4Type;
import app.nzyme.core.ethernet.tcp.TcpSessionState;
import org.jdbi.v3.core.mapper.RowMapper;
import org.jdbi.v3.core.statement.StatementContext;
import org.joda.time.DateTime;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.UUID;

public class TcpSessionEntryMapper implements RowMapper<TcpSessionEntry> {

    @Override
    public TcpSessionEntry map(ResultSet rs, StatementContext ctx) throws SQLException {
        return TcpSessionEntry.create(
                rs.getLong("id"),
                UUID.fromString(rs.getString("tap_uuid")),
                L4Type.TCP,
                rs.getString("source_mac"),
                rs.getString("source_address"),
                rs.getInt("source_port"),
                rs.getString("destination_mac"),
                rs.getString("destination_address"),
                rs.getInt("destination_port"),
                rs.getLong("bytes_count"),
                rs.getLong("segments_count"),
                new DateTime(rs.getTimestamp("start_time")),
                rs.getTimestamp("end_time") == null ? null : new DateTime(rs.getTimestamp("end_time")),
                new DateTime(rs.getTimestamp("most_recent_segment_time")),
                TcpSessionState.valueOf(rs.getString("state").toUpperCase()),
                new DateTime(rs.getTimestamp("created_at"))
        );
    }

}
