package app.nzyme.core.dot11.db;

import org.jdbi.v3.core.mapper.RowMapper;
import org.jdbi.v3.core.statement.StatementContext;
import org.joda.time.DateTime;

import java.sql.ResultSet;
import java.sql.SQLException;

public class ClientActivityHistogramEntryMapper implements RowMapper<ClientActivityHistogramEntry> {

    @Override
    public ClientActivityHistogramEntry map(ResultSet rs, StatementContext ctx) throws SQLException {
        return ClientActivityHistogramEntry.create(
                new DateTime(rs.getTimestamp("bucket")),
                rs.getLong("frames")
        );
    }

}
