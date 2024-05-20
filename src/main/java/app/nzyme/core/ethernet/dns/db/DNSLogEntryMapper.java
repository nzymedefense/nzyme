package app.nzyme.core.ethernet.dns.db;

import org.jdbi.v3.core.mapper.RowMapper;
import org.jdbi.v3.core.statement.StatementContext;
import org.joda.time.DateTime;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.UUID;

public class DNSLogEntryMapper implements RowMapper<DNSLogEntry> {

    @Override
    public DNSLogEntry map(ResultSet rs, StatementContext ctx) throws SQLException {
        return DNSLogEntry.create(
                rs.getLong("id"),
                UUID.fromString(rs.getString("uuid")),
                UUID.fromString(rs.getString("tap_uuid")),
                rs.getInt("transaction_id"),
                rs.getString("client_address"),
                rs.getInt("client_port"),
                rs.getString("client_mac"),
                rs.getString("server_address"),
                rs.getInt("server_port"),
                rs.getString("server_mac"),
                rs.getString("data_value"),
                rs.getString("data_value_etld"),
                rs.getString("data_type"),
                rs.getString("dns_type"),
                new DateTime(rs.getTimestamp("timestamp")),
                new DateTime(rs.getTimestamp("created_at"))
        );
    }

}
