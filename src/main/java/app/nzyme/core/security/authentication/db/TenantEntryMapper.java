package app.nzyme.core.security.authentication.db;

import org.jdbi.v3.core.mapper.RowMapper;
import org.jdbi.v3.core.statement.StatementContext;
import org.joda.time.DateTime;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.UUID;

public class TenantEntryMapper implements RowMapper<TenantEntry> {

    @Override
    public TenantEntry map(ResultSet rs, StatementContext ctx) throws SQLException {
        return TenantEntry.create(
                UUID.fromString(rs.getString("uuid")),
                UUID.fromString(rs.getString("organization_id")),
                rs.getString("name"),
                rs.getString("description"),
                rs.getInt("session_timeout_minutes"),
                rs.getInt("session_inactivity_timeout_minutes"),
                rs.getInt("mfa_timeout_minutes"),
                new DateTime(rs.getTimestamp("created_at")),
                new DateTime(rs.getTimestamp("updated_at"))
        );
    }

}