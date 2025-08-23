package app.nzyme.core.taps.db;

import app.nzyme.core.taps.Tap;
import app.nzyme.core.taps.TotalWithAverage;
import org.jdbi.v3.core.mapper.RowMapper;
import org.jdbi.v3.core.statement.StatementContext;
import org.joda.time.DateTime;
import org.joda.time.Period;
import org.joda.time.PeriodType;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.UUID;

public class TapMapper implements RowMapper<Tap>  {

    @Override
    public Tap map(ResultSet rs, StatementContext ctx) throws SQLException {
        DateTime updatedAt = new DateTime(rs.getTimestamp("updated_at"));
        DateTime clock = rs.getTimestamp("clock") == null
                ? null : new DateTime(rs.getTimestamp("clock"));
        DateTime lastReport = rs.getTimestamp("last_report") == null
                ? null : new DateTime(rs.getTimestamp("last_report"));

        UUID organizationId = rs.getString("organization_id") == null ?
                null : UUID.fromString(rs.getString("organization_id"));

        UUID tenantId = rs.getString("tenant_id") == null ?
                null : UUID.fromString(rs.getString("tenant_id"));

        UUID locationId = rs.getString("location_uuid") == null ?
                null : UUID.fromString(rs.getString("location_uuid"));
        UUID floorId = rs.getString("floor_uuid") == null ?
                null : UUID.fromString(rs.getString("floor_uuid"));

        return Tap.create(
                rs.getLong("id"),
                UUID.fromString(rs.getString("uuid")),
                rs.getString("name"),
                rs.getString("description"),
                rs.getString("version"),
                clock,
                TotalWithAverage.create(rs.getLong("processed_bytes_total"), rs.getLong("processed_bytes_average")),
                rs.getLong("memory_total"),
                rs.getLong("memory_free"),
                rs.getLong("memory_used"),
                rs.getDouble("cpu_load"),
                (long) new Period(lastReport, clock, PeriodType.millis()).getMillis(),
                rs.getString("rpi"),
                rs.getObject("rpi_temperature", Double.class),
                new DateTime(rs.getTimestamp("created_at")),
                updatedAt,
                lastReport,
                organizationId,
                tenantId,
                locationId,
                floorId,
                rs.getInt("floor_location_x"),
                rs.getInt("floor_location_y"),
                rs.getString("remote_address"),
                rs.getObject("latitude", Double.class),
                rs.getObject("longitude", Double.class)
        );
    }

}
