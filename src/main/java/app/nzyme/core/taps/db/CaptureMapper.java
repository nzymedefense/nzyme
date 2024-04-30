package app.nzyme.core.taps.db;

import app.nzyme.core.taps.Capture;
import org.jdbi.v3.core.mapper.RowMapper;
import org.jdbi.v3.core.statement.StatementContext;
import org.joda.time.DateTime;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.UUID;

public class CaptureMapper implements RowMapper<Capture> {

    @Override
    public Capture map(ResultSet rs, StatementContext ctx) throws SQLException {
        return Capture.create(
                UUID.fromString(rs.getString("uuid")),
                rs.getString("interface"),
                rs.getString("capture_type"),
                rs.getBoolean("is_running"),
                rs.getLong("received"),
                rs.getLong("dropped_buffer"),
                rs.getLong("dropped_interface"),
                rs.getInt("cycle_time"),
                new DateTime(rs.getTimestamp("created_at")),
                new DateTime(rs.getTimestamp("updated_at"))
        );
    }

}
