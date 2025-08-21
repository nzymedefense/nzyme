package app.nzyme.core.gnss.db;

import org.jdbi.v3.core.mapper.RowMapper;
import org.jdbi.v3.core.statement.StatementContext;
import org.joda.time.DateTime;

import java.sql.ResultSet;
import java.sql.SQLException;

public class GNSSDoubleBucketMapper implements RowMapper<GNSSDoubleBucket> {
    @Override
    public GNSSDoubleBucket map(ResultSet rs, StatementContext ctx) throws SQLException {
        return GNSSDoubleBucket.create(
                new DateTime(rs.getTimestamp("bucket")),
                rs.getObject("gps", Double.class),
                rs.getObject("glonass", Double.class),
                rs.getObject("beidou", Double.class),
                rs.getObject("galileo", Double.class)
        );
    }
}
