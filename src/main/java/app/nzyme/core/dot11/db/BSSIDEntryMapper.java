package app.nzyme.core.dot11.db;

import org.jdbi.v3.core.mapper.RowMapper;
import org.jdbi.v3.core.statement.StatementContext;
import org.joda.time.DateTime;

import java.sql.ResultSet;
import java.sql.SQLException;

public class BSSIDEntryMapper implements RowMapper<BSSIDEntry> {

    @Override
    public BSSIDEntry map(ResultSet rs, StatementContext ctx) throws SQLException {
        return BSSIDEntry.create(
                rs.getString("bssid"),
                rs.getFloat("signal_strength_average"),
                new DateTime(rs.getTimestamp("last_seen")),
                rs.getLong("hidden_ssid_frames")
        );
    }

}
