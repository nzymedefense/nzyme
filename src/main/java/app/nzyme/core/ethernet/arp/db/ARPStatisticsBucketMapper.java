package app.nzyme.core.ethernet.arp.db;

import org.jdbi.v3.core.mapper.RowMapper;
import org.jdbi.v3.core.statement.StatementContext;
import org.joda.time.DateTime;

import java.sql.ResultSet;
import java.sql.SQLException;

public class ARPStatisticsBucketMapper implements RowMapper<ARPStatisticsBucket> {

    @Override
    public ARPStatisticsBucket map(ResultSet rs, StatementContext ctx) throws SQLException {
        return ARPStatisticsBucket.create(
                new DateTime(rs.getTimestamp("bucket")),
                rs.getLong("total_count"),
                rs.getLong("request_count"),
                rs.getLong("reply_count"),
                rs.getDouble("request_to_reply_ratio"),
                rs.getLong("gratuitous_request_count"),
                rs.getLong("gratuitous_reply_count")
        );
    }

}
