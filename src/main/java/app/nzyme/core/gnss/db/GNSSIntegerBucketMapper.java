package app.nzyme.core.gnss.db;

import org.jdbi.v3.core.mapper.RowMapper;
import org.jdbi.v3.core.statement.StatementContext;
import org.joda.time.DateTime;

import java.sql.ResultSet;
import java.sql.SQLException;

public class GNSSIntegerBucketMapper implements RowMapper<GNSSIntegerBucket> {
    @Override
    public GNSSIntegerBucket map(ResultSet rs, StatementContext ctx) throws SQLException {
        return GNSSIntegerBucket.create(
                new DateTime(rs.getTimestamp("bucket")),
                rs.getInt("gps"),
                rs.getInt("glonass"),
                rs.getInt("beidou"),
                rs.getInt("galileo")
        );
    }
}
