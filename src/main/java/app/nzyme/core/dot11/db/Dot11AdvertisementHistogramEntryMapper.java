package app.nzyme.core.dot11.db;

import org.jdbi.v3.core.mapper.RowMapper;
import org.jdbi.v3.core.statement.StatementContext;
import org.joda.time.DateTime;

import java.sql.ResultSet;
import java.sql.SQLException;

public class Dot11AdvertisementHistogramEntryMapper implements RowMapper<Dot11AdvertisementHistogramEntry> {

    @Override
    public Dot11AdvertisementHistogramEntry map(ResultSet rs, StatementContext ctx) throws SQLException {
        return Dot11AdvertisementHistogramEntry.create(
                rs.getLong("beacons"),
                rs.getLong("proberesponses"),
                new DateTime(rs.getTimestamp("bucket"))
        );
    }

}
