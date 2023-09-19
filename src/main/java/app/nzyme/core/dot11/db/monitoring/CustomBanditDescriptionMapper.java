package app.nzyme.core.dot11.db.monitoring;


import org.jdbi.v3.core.mapper.RowMapper;
import org.jdbi.v3.core.statement.StatementContext;
import org.joda.time.DateTime;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.UUID;

public class CustomBanditDescriptionMapper implements RowMapper<CustomBanditDescription> {

    @Override
    public CustomBanditDescription map(ResultSet rs, StatementContext ctx) throws SQLException {
        return CustomBanditDescription.create(
                rs.getLong("id"),
                UUID.fromString(rs.getString("uuid")),
                UUID.fromString(rs.getString("organization_id")),
                UUID.fromString(rs.getString("tenant_id")),
                rs.getString("name"),
                rs.getString("description"),
                new DateTime(rs.getTimestamp("created_at")),
                new DateTime(rs.getTimestamp("updated_at"))
        );
    }

}
