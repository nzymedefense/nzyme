package app.nzyme.core.measurements.mappers;

import app.nzyme.core.database.DatabaseImpl;
import app.nzyme.core.measurements.Measurement;
import app.nzyme.core.measurements.MeasurementType;
import org.jdbi.v3.core.mapper.RowMapper;
import org.jdbi.v3.core.statement.StatementContext;
import org.joda.time.DateTime;

import java.sql.ResultSet;
import java.sql.SQLException;

public class MeasurementMapper implements RowMapper<Measurement> {

    @Override
    public Measurement map(ResultSet rs, StatementContext ctx) throws SQLException {
        return Measurement.create(
                MeasurementType.valueOf(rs.getString("measurement_type")),
                rs.getLong("measurement_value"),
                DateTime.parse(rs.getString("created_at"), DatabaseImpl.DATABASE_DATE_TIME_FORMATTER)
        );
    }

}
