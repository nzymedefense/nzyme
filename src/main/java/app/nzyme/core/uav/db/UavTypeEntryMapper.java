package app.nzyme.core.uav.db;

import org.jdbi.v3.core.mapper.RowMapper;
import org.jdbi.v3.core.statement.StatementContext;
import org.joda.time.DateTime;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.UUID;

public class UavTypeEntryMapper implements RowMapper<UavTypeEntry> {

    @Override
    public UavTypeEntry map(ResultSet rs, StatementContext ctx) throws SQLException {
        return UavTypeEntry.create(
                rs.getLong("id"),
                UUID.fromString(rs.getString("uuid")),
                UUID.fromString(rs.getString("organization_id")),
                UUID.fromString(rs.getString("tenant_id")),
                rs.getString("match_type"),
                rs.getString("match_value"),
                rs.getString("default_classification"),
                rs.getString("type"),
                rs.getString("name"),
                rs.getString("model"),
                new DateTime(rs.getTimestamp("created_at")),
                new DateTime(rs.getTimestamp("updated_at"))
        );
    }

}
