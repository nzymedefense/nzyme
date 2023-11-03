package app.nzyme.core.dot11.db;

import org.jdbi.v3.core.mapper.RowMapper;
import org.jdbi.v3.core.statement.StatementContext;

import java.sql.ResultSet;
import java.sql.SQLException;

public class BSSIDFrameCountMapper implements RowMapper<Dot11MacFrameCount> {

    @Override
    public Dot11MacFrameCount map(ResultSet rs, StatementContext ctx) throws SQLException {
        return Dot11MacFrameCount.create(
                rs.getString("bssid"),
                rs.getLong("frame_count")
        );
    }

}
