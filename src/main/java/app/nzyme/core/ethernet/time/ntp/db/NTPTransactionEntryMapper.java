package app.nzyme.core.ethernet.time.ntp.db;

import org.jdbi.v3.core.mapper.RowMapper;
import org.jdbi.v3.core.statement.StatementContext;
import org.joda.time.DateTime;

import java.sql.ResultSet;
import java.sql.SQLException;

public class NTPTransactionEntryMapper implements RowMapper<NTPTransactionEntry> {
    @Override
    public NTPTransactionEntry map(ResultSet rs, StatementContext ctx) throws SQLException {
        DateTime timestampClientTransmit = rs.getTimestamp("timestamp_client_transmit") == null ?
                null : new DateTime(rs.getTimestamp("timestamp_client_transmit"));
        DateTime timestampServerReceive = rs.getTimestamp("timestamp_server_receive") == null ?
                null : new DateTime(rs.getTimestamp("timestamp_server_receive"));
        DateTime timestampServerTransmit = rs.getTimestamp("timestamp_server_transmit") == null ?
                null : new DateTime(rs.getTimestamp("timestamp_server_transmit"));
        DateTime timestampClientTapReceive = rs.getTimestamp("timestamp_client_tap_receive") == null ?
                null : new DateTime(rs.getTimestamp("timestamp_client_tap_receive"));
        DateTime timestampServerTapReceive = rs.getTimestamp("timestamp_server_tap_receive") == null ?
                null : new DateTime(rs.getTimestamp("timestamp_server_tap_receive"));

        return NTPTransactionEntry.create(
                rs.getString("transaction_key"),
                rs.getBoolean("complete"),
                rs.getString("notes"),
                rs.getString("client_mac"),
                rs.getString("server_mac"),
                rs.getString("client_address"),
                rs.getString("server_address"),
                rs.getObject("client_port", Integer.class),
                rs.getObject("server_port", Integer.class),
                rs.getObject("request_size", Integer.class),
                rs.getObject("response_size", Integer.class),
                timestampClientTransmit,
                timestampServerReceive,
                timestampServerTransmit,
                timestampClientTapReceive,
                timestampServerTapReceive,
                rs.getObject("server_version", Integer.class),
                rs.getObject("client_version", Integer.class),
                rs.getObject("server_mode", Integer.class),
                rs.getObject("client_mode", Integer.class),
                rs.getObject("stratum", Integer.class),
                rs.getObject("leap_indicator", Integer.class),
                rs.getObject("precision", Long.class),
                rs.getObject("poll_interval", Long.class),
                rs.getObject("root_delay_seconds", Double.class),
                rs.getObject("root_dispersion_seconds", Double.class),
                rs.getObject("delay_seconds", Double.class),
                rs.getObject("offset_seconds", Double.class),
                rs.getObject("rtt_seconds", Double.class),
                rs.getObject("server_processing_seconds", Double.class),
                rs.getString("reference_id"),
                new DateTime(rs.getTimestamp("created_at"))
        );
    }
}
