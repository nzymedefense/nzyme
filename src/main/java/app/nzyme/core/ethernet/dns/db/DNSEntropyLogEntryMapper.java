package app.nzyme.core.ethernet.dns.db;

import org.jdbi.v3.core.mapper.RowMapper;
import org.jdbi.v3.core.statement.StatementContext;
import org.joda.time.DateTime;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.UUID;

public class DNSEntropyLogEntryMapper implements RowMapper<DNSEntropyLogEntry> {

    @Override
    public DNSEntropyLogEntry map(ResultSet rs, StatementContext ctx) throws SQLException {
        return DNSEntropyLogEntry.create(
                rs.getLong("id"),
                rs.getInt("transaction_id"),
                rs.getFloat("entropy"),
                rs.getFloat("entropy_mean"),
                rs.getFloat("zscore"),
                new DateTime(rs.getTimestamp("timestamp")),
                new DateTime(rs.getTimestamp("created_at")),
                UUID.fromString(rs.getString("tap_uuid"))
        );
    }

}
