package app.nzyme.core.security.authentication.db;

import org.jdbi.v3.core.mapper.RowMapper;
import org.jdbi.v3.core.statement.StatementContext;
import org.joda.time.DateTime;

import java.sql.ResultSet;
import java.sql.SQLException;

public class TapPermissionEntryMapper implements RowMapper<TapPermissionEntry> {

    @Override
    public TapPermissionEntry map(ResultSet rs, StatementContext ctx) throws SQLException {


        return TapPermissionEntry.create(
                rs.getLong("id"),
                rs.getLong("organization_id"),
                rs.getLong("tenant_id"),
                rs.getString("name"),
                rs.getString("description"),
                rs.getString("secret"),
                new DateTime(rs.getTimestamp("created_at")),
                new DateTime(rs.getTimestamp("updated_at")),
                rs.getTimestamp("last_report") == null ?
                        null : new DateTime(rs.getTimestamp("last_report"))
        );
    }

}
