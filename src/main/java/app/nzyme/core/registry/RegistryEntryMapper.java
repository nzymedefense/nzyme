package app.nzyme.core.registry;

import org.jdbi.v3.core.mapper.RowMapper;
import org.jdbi.v3.core.statement.StatementContext;

import java.sql.ResultSet;
import java.sql.SQLException;

public class RegistryEntryMapper implements RowMapper<RegistryEntry> {

    @Override
    public RegistryEntry map(ResultSet rs, StatementContext ctx) throws SQLException {
        return RegistryEntry.create(
                rs.getString("key"),
                rs.getString("value")
        );
    }

}
