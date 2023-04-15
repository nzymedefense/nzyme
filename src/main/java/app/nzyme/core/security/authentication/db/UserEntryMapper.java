package app.nzyme.core.security.authentication.db;

import org.jdbi.v3.core.mapper.RowMapper;
import org.jdbi.v3.core.statement.StatementContext;
import org.joda.time.DateTime;

import java.sql.ResultSet;
import java.sql.SQLException;

public class UserEntryMapper implements RowMapper<UserEntry> {

    @Override
    public UserEntry map(ResultSet rs, StatementContext ctx) throws SQLException {
        long roleId = rs.getLong("role_id");

        return UserEntry.create(
                rs.getLong("id"),
                rs.getLong("organization_id"),
                rs.getLong("tenant_id"),
                roleId == 0 ? null : roleId,
                rs.getString("email"),
                rs.getString("name"),
                rs.getBoolean("is_orgadmin"),
                rs.getBoolean("is_superadmin"),
                new DateTime(rs.getTimestamp("updated_at")),
                new DateTime(rs.getTimestamp("created_at"))
        );
    }

}
