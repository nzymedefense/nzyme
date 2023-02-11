package app.nzyme.core.ethernet.dns.db;

import org.jdbi.v3.core.mapper.RowMapper;
import org.jdbi.v3.core.statement.StatementContext;
import org.joda.time.DateTime;

import java.sql.ResultSet;
import java.sql.SQLException;

public class DNSStatisticsBucketMapper implements RowMapper<DNSStatisticsBucket> {

    @Override
    public DNSStatisticsBucket map(ResultSet rs, StatementContext ctx) throws SQLException {
        return DNSStatisticsBucket.create(
                new DateTime(rs.getTimestamp("bucket")),
                rs.getLong("request_count"),
                rs.getLong("request_bytes"),
                rs.getLong("response_count"),
                rs.getLong("response_bytes"),
                rs.getLong("nxdomain_count")
        );
    }

}
