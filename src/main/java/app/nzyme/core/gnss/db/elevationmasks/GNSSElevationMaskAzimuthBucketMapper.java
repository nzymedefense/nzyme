package app.nzyme.core.gnss.db.elevationmasks;

import org.jdbi.v3.core.mapper.RowMapper;
import org.jdbi.v3.core.statement.StatementContext;
import org.joda.time.DateTime;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.UUID;

public class GNSSElevationMaskAzimuthBucketMapper implements RowMapper<GNSSElevationMaskAzimuthBucket> {

    @Override
    public GNSSElevationMaskAzimuthBucket map(ResultSet rs, StatementContext ctx) throws SQLException {
        return GNSSElevationMaskAzimuthBucket.create(
                UUID.fromString(rs.getString("tap_uuid")),
                rs.getInt("azimuth_bucket"),
                rs.getDouble("skyline_elevation") == 0 ? null : rs.getDouble("skyline_elevation"),
                rs.getDouble("skyline_elevation_best_effort"),
                rs.getInt("low_subset_count"),
                rs.getDouble("min_elevation_observed"),
                rs.getBoolean("used_fallback"),
                rs.getDouble("sno_median"),
                rs.getDouble("sno_p10"),
                rs.getInt("sample_count"),
                new DateTime(rs.getTimestamp("window_start")),
                new DateTime(rs.getTimestamp("window_end"))
        );
    }

}
