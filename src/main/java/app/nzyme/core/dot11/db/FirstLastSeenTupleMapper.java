package app.nzyme.core.dot11.db;

import org.jdbi.v3.core.mapper.RowMapper;
import org.jdbi.v3.core.statement.StatementContext;
import org.joda.time.DateTime;

import java.sql.ResultSet;
import java.sql.SQLException;

public class FirstLastSeenTupleMapper implements RowMapper<FirstLastSeenTuple> {

    @Override
    public FirstLastSeenTuple map(ResultSet rs, StatementContext ctx) throws SQLException {
        DateTime firstSeen = rs.getTimestamp("first_seen") == null ?
                null : new DateTime(rs.getTimestamp("first_seen"));

        DateTime lastSeen = rs.getTimestamp("last_seen") == null ?
                null : new DateTime(rs.getTimestamp("last_seen"));

        return FirstLastSeenTuple.create(firstSeen, lastSeen);
    }

}
