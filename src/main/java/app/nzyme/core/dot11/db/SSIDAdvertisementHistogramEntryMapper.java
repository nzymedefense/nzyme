package app.nzyme.core.dot11.db;

import org.jdbi.v3.core.mapper.RowMapper;
import org.jdbi.v3.core.statement.StatementContext;
import org.joda.time.DateTime;

import java.sql.ResultSet;
import java.sql.SQLException;

public class SSIDAdvertisementHistogramEntryMapper implements RowMapper<SSIDAdvertisementHistogramEntry> {

    @Override
    public SSIDAdvertisementHistogramEntry map(ResultSet rs, StatementContext ctx) throws SQLException {
        return SSIDAdvertisementHistogramEntry.create(
                rs.getLong("beacons"),
                rs.getLong("proberesponses"),
                new DateTime(rs.getTimestamp("bucket"))
        );
    }

}
