package app.nzyme.core.distributed.database;

import org.jdbi.v3.core.mapper.RowMapper;
import org.jdbi.v3.core.statement.StatementContext;
import org.joda.time.DateTime;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.UUID;

public class NodeEntryMapper implements RowMapper<NodeEntry> {

    @Override
    public NodeEntry map(ResultSet rs, StatementContext ctx) throws SQLException {
        return NodeEntry.create(
                UUID.fromString(rs.getString("uuid")),
                rs.getString("name"),
                rs.getString("http_listen_uri"),
                rs.getString("http_external_uri"),
                rs.getLong("memory_bytes_total"),
                rs.getLong("memory_bytes_available"),
                rs.getLong("memory_bytes_used"),
                rs.getLong("heap_bytes_total"),
                rs.getLong("heap_bytes_available"),
                rs.getLong("heap_bytes_used"),
                rs.getDouble("cpu_system_load"),
                rs.getInt("cpu_thread_count"),
                new DateTime(rs.getTimestamp("process_start_time")),
                rs.getLong("process_virtual_size"),
                rs.getString("process_arguments"),
                rs.getString("os_information"),
                rs.getString("version"),
                new DateTime(rs.getTimestamp("last_seen")),
                new DateTime(rs.getTimestamp("clock")),
                rs.getBoolean("deleted"),
                rs.getLong("cycle")
        );
    }

}
