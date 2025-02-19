package app.nzyme.core.uav.db;

import org.jdbi.v3.core.mapper.RowMapper;
import org.jdbi.v3.core.statement.StatementContext;
import org.joda.time.DateTime;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.UUID;

public class UavEntryMapper implements RowMapper<UavEntry> {
    @Override
    public UavEntry map(ResultSet rs, StatementContext ctx) throws SQLException {
        return UavEntry.create(
                rs.getLong("id"),
                UUID.fromString(rs.getString("tap_uuid")),
                rs.getString("identifier"),
                rs.getString("designation"),
                rs.getString("uav_type"),
                rs.getString("detection_source"),
                rs.getString("id_serial"),
                rs.getString("id_registration"),
                rs.getString("id_utm"),
                rs.getString("id_session"),
                rs.getDouble("rssi_average"),
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
                rs.getString("operator_location_type"),
                rs.getDouble("operator_latitude"),
                rs.getDouble("operator_longitude"),
                rs.getDouble("operator_altitude"),
                new DateTime(rs.getTimestamp("first_seen")),
                new DateTime(rs.getTimestamp("last_seen"))
        );
    }
}
