package app.nzyme.core.ethernet.dhcp.db;

import org.jdbi.v3.core.mapper.RowMapper;
import org.jdbi.v3.core.statement.StatementContext;
import org.joda.time.DateTime;

import java.sql.ResultSet;
import java.sql.SQLException;

public class DHCPStatisticsBucketMapper implements RowMapper<DHCPStatisticsBucket> {

    @Override
    public DHCPStatisticsBucket map(ResultSet rs, StatementContext ctx) throws SQLException {
        return DHCPStatisticsBucket.create(
                new DateTime(rs.getTimestamp("bucket")),
                rs.getLong("total_transaction_count"),
                rs.getLong("successful_transaction_count"),
                rs.getLong("failed_transaction_count")
        );
    }

}
