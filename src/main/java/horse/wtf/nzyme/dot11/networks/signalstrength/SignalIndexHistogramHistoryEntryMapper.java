package horse.wtf.nzyme.dot11.networks.signalstrength;

import horse.wtf.nzyme.database.Database;
import org.jdbi.v3.core.mapper.RowMapper;
import org.jdbi.v3.core.statement.StatementContext;
import org.joda.time.DateTime;

import java.sql.ResultSet;
import java.sql.SQLException;

public class SignalIndexHistogramHistoryEntryMapper implements RowMapper<SignalIndexHistogramHistoryEntry> {

    @Override
    public SignalIndexHistogramHistoryEntry map(ResultSet rs, StatementContext ctx) throws SQLException {
        return SignalIndexHistogramHistoryEntry.create(
                rs.getString("histogram"),
                DateTime.parse(rs.getString("created_at"), Database.DATE_TIME_FORMATTER)
        );

    }

}