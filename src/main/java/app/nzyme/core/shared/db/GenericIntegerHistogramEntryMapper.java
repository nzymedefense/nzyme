package app.nzyme.core.shared.db;

import org.jdbi.v3.core.mapper.RowMapper;
import org.jdbi.v3.core.statement.StatementContext;
import org.joda.time.DateTime;

import java.sql.ResultSet;
import java.sql.SQLException;

public class GenericIntegerHistogramEntryMapper implements RowMapper<GenericIntegerHistogramEntry> {

    @Override
    public GenericIntegerHistogramEntry map(ResultSet rs, StatementContext ctx) throws SQLException {
        return GenericIntegerHistogramEntry.create(
                new DateTime(rs.getTimestamp("bucket")),
                rs.getInt("value")
        );
    }

}
