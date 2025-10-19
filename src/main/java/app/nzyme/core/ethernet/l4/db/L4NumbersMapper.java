package app.nzyme.core.ethernet.l4.db;

import org.jdbi.v3.core.mapper.RowMapper;
import org.jdbi.v3.core.statement.StatementContext;

import java.sql.ResultSet;
import java.sql.SQLException;

public class L4NumbersMapper implements RowMapper<L4Numbers> {

    @Override
    public L4Numbers map(ResultSet rs, StatementContext ctx) throws SQLException {
        return L4Numbers.create(
                rs.getLong("bytes_tcp"),
                rs.getLong("bytes_internal_tcp"),
                rs.getLong("bytes_udp"),
                rs.getLong("bytes_internal_udp"),
                rs.getLong("segments_tcp"),
                rs.getLong("datagrams_udp")
        );
    }

}
