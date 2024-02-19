package app.nzyme.core.dot11.db;

import org.jdbi.v3.core.mapper.RowMapper;
import org.jdbi.v3.core.statement.StatementContext;
import org.joda.time.DateTime;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.UUID;

public class TapBasedSignalStrengthResultHistogramEntryMapper implements RowMapper<TapBasedSignalStrengthResultHistogramEntry> {

    @Override
    public TapBasedSignalStrengthResultHistogramEntry map(ResultSet rs, StatementContext ctx) throws SQLException {
        return TapBasedSignalStrengthResultHistogramEntry.create(
                new DateTime(rs.getTimestamp("bucket")),
                UUID.fromString(rs.getString("tap_uuid")),
                rs.getString("tap_name"),
                rs.getFloat("signal_strength")
        );
    }

}
