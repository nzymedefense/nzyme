package app.nzyme.core.context.db;

import org.jdbi.v3.core.mapper.RowMapper;
import org.jdbi.v3.core.statement.StatementContext;
import org.joda.time.DateTime;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.UUID;

public class MacAddressTransparentContextEntryMapper implements RowMapper<MacAddressTransparentContextEntry> {

    @Override
    public MacAddressTransparentContextEntry map(ResultSet rs, StatementContext ctx) throws SQLException {
        try {
            return MacAddressTransparentContextEntry.create(
                    rs.getLong("id"),
                    rs.getLong("context_id"),
                    UUID.fromString(rs.getString("tap_uuid")),
                    rs.getString("type"),
                    rs.getString("ip_address") == null ? null
                            : InetAddress.getByName(rs.getString("ip_address")),
                    rs.getString("hostname"),
                    rs.getString("source"),
                    new DateTime(rs.getTimestamp("last_seen")),
                    new DateTime(rs.getTimestamp("created_at"))
            );
        } catch (UnknownHostException e) {
            throw new RuntimeException(e);
        }
    }

}
