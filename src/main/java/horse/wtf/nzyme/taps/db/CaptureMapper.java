package horse.wtf.nzyme.taps.db;

import horse.wtf.nzyme.taps.Capture;
import org.jdbi.v3.core.mapper.RowMapper;
import org.jdbi.v3.core.statement.StatementContext;
import org.joda.time.DateTime;

import java.sql.ResultSet;
import java.sql.SQLException;

public class CaptureMapper implements RowMapper<Capture> {

    @Override
    public Capture map(ResultSet rs, StatementContext ctx) throws SQLException {
        return Capture.create(
                rs.getString("interface"),
                rs.getString("capture_type"),
                rs.getBoolean("is_running"),
                rs.getLong("received"),
                rs.getLong("dropped_buffer"),
                rs.getLong("dropped_interface"),
                new DateTime(rs.getTimestamp("created_at")),
                new DateTime(rs.getTimestamp("updated_at"))
        );
    }

}
