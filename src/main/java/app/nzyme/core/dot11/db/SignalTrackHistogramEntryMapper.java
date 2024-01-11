package app.nzyme.core.dot11.db;

import org.jdbi.v3.core.mapper.RowMapper;
import org.jdbi.v3.core.statement.StatementContext;
import org.joda.time.DateTime;

import java.sql.ResultSet;
import java.sql.SQLException;

public class SignalTrackHistogramEntryMapper implements RowMapper<SignalTrackHistogramEntry> {

    @Override
    public SignalTrackHistogramEntry map(ResultSet rs, StatementContext ctx) throws SQLException {
        return SignalTrackHistogramEntry.create(
                new DateTime(rs.getTimestamp("bucket")),
                rs.getInt("signal_strength"),
                rs.getLong("frame_count")
        );
    }

}
