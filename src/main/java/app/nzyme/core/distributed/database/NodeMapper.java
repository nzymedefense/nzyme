package app.nzyme.core.distributed.database;

import org.jdbi.v3.core.mapper.RowMapper;
import org.jdbi.v3.core.statement.StatementContext;
import org.joda.time.DateTime;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.UUID;

public class NodeMapper implements RowMapper<NodeEntry> {

    @Override
    public NodeEntry map(ResultSet rs, StatementContext ctx) throws SQLException {
        return NodeEntry.create(
                UUID.fromString(rs.getString("uuid")),
                rs.getString("name"),
                rs.getString("transport_address"),
                rs.getString("version"),
                new DateTime(rs.getTimestamp("last_seen"))
        );
    }

}
