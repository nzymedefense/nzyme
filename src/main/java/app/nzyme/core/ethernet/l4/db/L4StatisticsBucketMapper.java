package app.nzyme.core.ethernet.l4.db;

import org.jdbi.v3.core.mapper.RowMapper;
import org.jdbi.v3.core.statement.StatementContext;
import org.joda.time.DateTime;

import java.sql.ResultSet;
import java.sql.SQLException;

public class L4StatisticsBucketMapper implements RowMapper<L4StatisticsBucket> {

    @Override
    public L4StatisticsBucket map(ResultSet rs, StatementContext ctx) throws SQLException {
        return L4StatisticsBucket.create(
                new DateTime(rs.getTimestamp("bucket")),
                rs.getLong("bytes_tcp"),
                rs.getLong("bytes_internal_tcp"),
                rs.getLong("bytes_udp"),
                rs.getLong("bytes_internal_udp"),
                rs.getLong("segments_tcp"),
                rs.getLong("datagrams_udp"),
                rs.getLong("sessions_tcp"),
                rs.getLong("sessions_udp"),
                rs.getLong("sessions_internal_tcp"),
                rs.getLong("sessions_internal_udp")
        );
    }

}
