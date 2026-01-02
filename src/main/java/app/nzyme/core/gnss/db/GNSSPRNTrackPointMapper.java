package app.nzyme.core.gnss.db;

import org.jdbi.v3.core.mapper.RowMapper;
import org.jdbi.v3.core.statement.StatementContext;
import org.joda.time.DateTime;

import java.sql.ResultSet;
import java.sql.SQLException;

public class GNSSPRNTrackPointMapper implements RowMapper<GNSSPRNTrackPoint> {
    @Override
    public GNSSPRNTrackPoint map(ResultSet rs, StatementContext ctx) throws SQLException {
        return GNSSPRNTrackPoint.create(
                rs.getInt("average_sno"),
                rs.getInt("azimuth_degrees"),
                rs.getInt("elevation_degrees"),
                new DateTime(rs.getTimestamp("timestamp"))
        );
    }
}
