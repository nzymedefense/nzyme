package app.nzyme.core.gnss.db;

import org.jdbi.v3.core.mapper.RowMapper;
import org.jdbi.v3.core.statement.StatementContext;
import org.joda.time.DateTime;

import java.sql.ResultSet;
import java.sql.SQLException;

public class GNSSStringBucketMapper implements RowMapper<GNSSStringBucket> {

    @Override
    public GNSSStringBucket map(ResultSet rs, StatementContext ctx) throws SQLException {
        return GNSSStringBucket.create(
                new DateTime(rs.getTimestamp("bucket")),
                rs.getString("gps"),
                rs.getString("glonass"),
                rs.getString("beidou"),
                rs.getString("galileo")
        );
    }

}
