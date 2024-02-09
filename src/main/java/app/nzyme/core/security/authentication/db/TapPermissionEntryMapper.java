package app.nzyme.core.security.authentication.db;

import org.jdbi.v3.core.mapper.RowMapper;
import org.jdbi.v3.core.statement.StatementContext;
import org.joda.time.DateTime;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.UUID;

public class TapPermissionEntryMapper implements RowMapper<TapPermissionEntry> {


    @Override
    public TapPermissionEntry map(ResultSet rs, StatementContext ctx) throws SQLException {
        String organizationId = rs.getString("organization_id");
        String tenantId = rs.getString("tenant_id");

        String locationUuid = rs.getString("location_uuid");
        String floorUuid = rs.getString("floor_uuid");

        return TapPermissionEntry.create(
                rs.getLong("id"),
                UUID.fromString(rs.getString("uuid")),
                organizationId == null ? null : UUID.fromString(organizationId),
                tenantId == null ? null : UUID.fromString(tenantId),
                rs.getString("name"),
                rs.getString("description"),
                rs.getString("secret"),
                locationUuid == null ? null : UUID.fromString(locationUuid),
                floorUuid == null ? null : UUID.fromString(floorUuid),
                rs.getInt("floor_location_x"),
                rs.getInt("floor_location_y"),
                new DateTime(rs.getTimestamp("created_at")),
                new DateTime(rs.getTimestamp("updated_at")),
                rs.getTimestamp("last_report") == null ?
                        null : new DateTime(rs.getTimestamp("last_report"))
        );
    }

}
