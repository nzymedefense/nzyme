package app.nzyme.core.ethernet.arp.db;

import org.jdbi.v3.core.mapper.RowMapper;
import org.jdbi.v3.core.statement.StatementContext;

import java.sql.ResultSet;
import java.sql.SQLException;

public class ArpSenderTargetCountPairMapper implements RowMapper<ArpSenderTargetCountPair> {

    @Override
    public ArpSenderTargetCountPair map(ResultSet rs, StatementContext ctx) throws SQLException {
        return ArpSenderTargetCountPair.create(
                rs.getString("arp_sender_mac"),
                rs.getString("arp_target_mac"),
                rs.getLong("count")
        );
    }

}
