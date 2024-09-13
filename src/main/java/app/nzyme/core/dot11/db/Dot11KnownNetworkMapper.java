package app.nzyme.core.dot11.db;

import org.jdbi.v3.core.mapper.RowMapper;
import org.jdbi.v3.core.statement.StatementContext;
import org.joda.time.DateTime;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.UUID;

public class Dot11KnownNetworkMapper implements RowMapper<Dot11KnownNetwork> {

    @Override
    public Dot11KnownNetwork map(ResultSet rs, StatementContext ctx) throws SQLException {
        return Dot11KnownNetwork.create(
                rs.getLong("id"),
                UUID.fromString(rs.getString("uuid")),
                rs.getString("ssid"),
                rs.getBoolean("is_approved"),
                UUID.fromString(rs.getString("organization_id")),
                UUID.fromString(rs.getString("tenant_id")),
                new DateTime(rs.getTimestamp("first_seen")),
                new DateTime(rs.getTimestamp("last_seen"))
        );
    }

}
