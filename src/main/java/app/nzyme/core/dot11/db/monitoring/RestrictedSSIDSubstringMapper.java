package app.nzyme.core.dot11.db.monitoring;

import org.jdbi.v3.core.mapper.RowMapper;
import org.jdbi.v3.core.statement.StatementContext;
import org.joda.time.DateTime;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.UUID;

public class RestrictedSSIDSubstringMapper implements RowMapper<RestrictedSSIDSubstring> {

    @Override
    public RestrictedSSIDSubstring map(ResultSet rs, StatementContext ctx) throws SQLException {
        return RestrictedSSIDSubstring.create(
                rs.getLong("id"),
                UUID.fromString(rs.getString("uuid")),
                rs.getString("substring"),
                new DateTime(rs.getTimestamp("created_at"))
        );
    }

}
