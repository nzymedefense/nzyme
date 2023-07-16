package app.nzyme.core.dot11.db;

import org.jdbi.v3.core.mapper.RowMapper;
import org.jdbi.v3.core.statement.StatementContext;
import org.joda.time.DateTime;

import java.sql.ResultSet;
import java.sql.SQLException;

public class ClientHistogramEntryMapper implements RowMapper<ClientHistogramEntry> {

    @Override
    public ClientHistogramEntry map(ResultSet rs, StatementContext ctx) throws SQLException {
        return ClientHistogramEntry.create(
                new DateTime(rs.getTimestamp("bucket")),
                rs.getLong("client_count")
        );
    }

}
