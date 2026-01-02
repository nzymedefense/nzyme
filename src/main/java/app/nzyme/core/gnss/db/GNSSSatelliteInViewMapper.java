package app.nzyme.core.gnss.db;

import org.jdbi.v3.core.mapper.RowMapper;
import org.jdbi.v3.core.statement.StatementContext;
import org.joda.time.DateTime;

import java.sql.ResultSet;
import java.sql.SQLException;

public class GNSSSatelliteInViewMapper implements RowMapper<GNSSSatelliteInView> {

    @Override
    public GNSSSatelliteInView map(ResultSet rs, StatementContext ctx) throws SQLException {
        return GNSSSatelliteInView.create(
                rs.getString("constellation"),
                new DateTime(rs.getTimestamp("last_seen")),
                rs.getInt("prn"),
                rs.getObject("average_sno", Integer.class),
                rs.getObject("azimuth_degrees", Integer.class),
                rs.getObject("elevation_degrees", Integer.class),
                rs.getBoolean("used_for_fix"),
                rs.getInt("average_doppler_hz"),
                rs.getInt("maximum_multipath_indicator"),
                rs.getInt("average_pseurange_rms_err")
        );
    }

}
