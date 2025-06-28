package app.nzyme.core.ethernet.udp.db;

import app.nzyme.core.ethernet.L4MapperTools;
import app.nzyme.core.ethernet.L4Type;
import app.nzyme.core.ethernet.udp.UdpConversationState;
import org.jdbi.v3.core.mapper.RowMapper;
import org.jdbi.v3.core.statement.StatementContext;
import org.joda.time.DateTime;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.UUID;

public class UdpConversationEntryMapper implements RowMapper<UdpConversationEntry> {

    @Override
    public UdpConversationEntry map(ResultSet rs, StatementContext ctx) throws SQLException {
        return UdpConversationEntry.create(
                rs.getLong("id"),
                rs.getString("session_key"),
                UUID.fromString(rs.getString("tap_uuid")),
                L4Type.TCP,
                L4MapperTools.fieldsToAddressData("source", rs),
                L4MapperTools.fieldsToAddressData("destination", rs),
                rs.getLong("bytes_count"),
                rs.getLong("segments_count"),
                new DateTime(rs.getTimestamp("start_time")),
                rs.getTimestamp("end_time") == null ? null : new DateTime(rs.getTimestamp("end_time")),
                new DateTime(rs.getTimestamp("most_recent_segment_time")),
                UdpConversationState.valueOf(rs.getString("state").toUpperCase()),
                new DateTime(rs.getTimestamp("created_at"))
        );
    }

}
