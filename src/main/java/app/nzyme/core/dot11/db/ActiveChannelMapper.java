package app.nzyme.core.dot11.db;


import org.jdbi.v3.core.mapper.RowMapper;
import org.jdbi.v3.core.statement.StatementContext;

import java.sql.ResultSet;
import java.sql.SQLException;

public class ActiveChannelMapper implements RowMapper<ActiveChannel> {

    @Override
    public ActiveChannel map(ResultSet rs, StatementContext ctx) throws SQLException {
        return ActiveChannel.create(
                rs.getInt("frequency"),
                rs.getLong("frames"),
                rs.getLong("bytes")
        );
    }

}
