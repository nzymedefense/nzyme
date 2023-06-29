package app.nzyme.core.dot11.db;

import org.jdbi.v3.core.mapper.RowMapper;
import org.jdbi.v3.core.statement.StatementContext;
import org.joda.time.DateTime;

import java.sql.ResultSet;
import java.sql.SQLException;

public class BSSIDAndSSIDCountHistogramEntryMapper implements RowMapper<BSSIDAndSSIDCountHistogramEntry> {

    @Override
    public BSSIDAndSSIDCountHistogramEntry map(ResultSet rs, StatementContext ctx) throws SQLException {
        return BSSIDAndSSIDCountHistogramEntry.create(
                rs.getLong("bssid_count"),
                rs.getLong(("ssid_count")),
                new DateTime(rs.getTimestamp("bucket"))
        );
    }

}
