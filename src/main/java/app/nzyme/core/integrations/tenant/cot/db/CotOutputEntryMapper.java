package app.nzyme.core.integrations.tenant.cot.db;

import org.jdbi.v3.core.mapper.RowMapper;
import org.jdbi.v3.core.statement.StatementContext;
import org.joda.time.DateTime;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.UUID;

public class CotOutputEntryMapper implements RowMapper<CotOutputEntry> {

    @Override
    public CotOutputEntry map(ResultSet rs, StatementContext ctx) throws SQLException {
        return CotOutputEntry.create(
                rs.getLong("id"),
                UUID.fromString(rs.getString("uuid")),
                UUID.fromString(rs.getString("organization_id")),
                UUID.fromString(rs.getString("tenant_id")),
                rs.getString("name"),
                rs.getString("description"),
                rs.getString("leaf_type_tap"),
                rs.getString("leaf_type_uav"),
                rs.getString("address"),
                rs.getInt("port"),
                new DateTime(rs.getTimestamp("updated_at")),
                new DateTime(rs.getTimestamp("created_at"))
        );
    }

}
