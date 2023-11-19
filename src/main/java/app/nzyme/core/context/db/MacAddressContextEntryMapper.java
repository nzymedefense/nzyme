package app.nzyme.core.context.db;

import org.jdbi.v3.core.mapper.RowMapper;
import org.jdbi.v3.core.statement.StatementContext;
import org.joda.time.DateTime;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.UUID;

public class MacAddressContextEntryMapper implements RowMapper<MacAddressContextEntry> {

    @Override
    public MacAddressContextEntry map(ResultSet rs, StatementContext ctx) throws SQLException {
        return MacAddressContextEntry.create(
                rs.getLong("id"),
                rs.getString("mac_address"),
                rs.getString("subsystem"),
                rs.getString("name"),
                rs.getString("description"),
                rs.getString("notes"),
                UUID.fromString(rs.getString("organization_id")),
                UUID.fromString("tenant_id"),
                new DateTime(rs.getTimestamp("created_at")),
                new DateTime(rs.getTimestamp("updated_at"))
        );
    }

}
