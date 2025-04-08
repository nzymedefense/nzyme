package app.nzyme.core.uav.db;

import org.jdbi.v3.core.mapper.RowMapper;
import org.jdbi.v3.core.statement.StatementContext;
import org.joda.time.DateTime;

import java.sql.ResultSet;
import java.sql.SQLException;

public class UavVectorEntryMapper implements RowMapper<UavVectorEntry> {

    @Override
    public UavVectorEntry map(ResultSet rs, StatementContext ctx) throws SQLException {
        return UavVectorEntry.create(
                rs.getLong("id"),
                rs.getLong("uav_id"),
                rs.getString("operational_status"),
                rs.getDouble("latitude"),
                rs.getDouble("longitude"),
                rs.getInt("ground_track"),
                rs.getDouble("speed"),
                rs.getDouble("vertical_speed"),
                rs.getDouble("altitude_pressure"),
                rs.getDouble("altitude_geodetic"),
                rs.getString("height_type"),
                rs.getDouble("height"),
                rs.getInt("accuracy_horizontal"),
                rs.getInt("accuracy_vertical"),
                rs.getInt("accuracy_barometer"),
                rs.getInt("accuracy_speed"),
                new DateTime(rs.getTimestamp("timestamp"))
        );
    }

}
