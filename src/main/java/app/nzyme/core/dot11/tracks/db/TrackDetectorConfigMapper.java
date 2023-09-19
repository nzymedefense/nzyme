package app.nzyme.core.dot11.tracks.db;

import org.jdbi.v3.core.mapper.RowMapper;
import org.jdbi.v3.core.statement.StatementContext;

import java.sql.ResultSet;
import java.sql.SQLException;

public class TrackDetectorConfigMapper implements RowMapper<TrackDetectorConfig> {

    @Override
    public TrackDetectorConfig map(ResultSet rs, StatementContext ctx) throws SQLException {
        return TrackDetectorConfig.create(
                rs.getInt("frame_threshold"),
                rs.getInt("gap_threshold"),
                rs.getInt("signal_centerline_jitter")
        );
    }

}