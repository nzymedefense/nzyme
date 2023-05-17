package app.nzyme.core.security.authentication.db;

import org.jdbi.v3.core.mapper.RowMapper;
import org.jdbi.v3.core.statement.StatementContext;
import org.joda.time.DateTime;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.UUID;

public class OrganizationEntryMapper implements RowMapper<OrganizationEntry> {

    @Override
    public OrganizationEntry map(ResultSet rs, StatementContext ctx) throws SQLException {
        return OrganizationEntry.create(
                UUID.fromString(rs.getString("uuid")),
                rs.getString("name"),
                rs.getString("description"),
                new DateTime(rs.getTimestamp("created_at")),
                new DateTime(rs.getTimestamp("updated_at"))
        );
    }

}
