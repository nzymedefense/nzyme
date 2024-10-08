package app.nzyme.core.dot11.db;

import org.jdbi.v3.core.mapper.RowMapper;
import org.jdbi.v3.core.statement.StatementContext;
import org.joda.time.DateTime;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.UUID;

public class Dot11KnownClientMapper implements RowMapper<Dot11KnownClient> {

    @Override
    public Dot11KnownClient map(ResultSet rs, StatementContext ctx) throws SQLException {
        return Dot11KnownClient.create(
                rs.getLong("id"),
                UUID.fromString(rs.getString("uuid")),
                rs.getString("mac"),
                rs.getBoolean("is_approved"),
                rs.getBoolean("is_ignored"),
                rs.getLong("monitored_network_id"),
                new DateTime(rs.getTimestamp("first_seen")),
                new DateTime(rs.getTimestamp("last_seen"))
        );
    }

}
