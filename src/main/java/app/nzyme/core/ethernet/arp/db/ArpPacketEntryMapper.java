package app.nzyme.core.ethernet.arp.db;

import org.jdbi.v3.core.mapper.RowMapper;
import org.jdbi.v3.core.statement.StatementContext;
import org.joda.time.DateTime;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.UUID;

public class ArpPacketEntryMapper implements RowMapper<ArpPacketEntry> {

    @Override
    public ArpPacketEntry map(ResultSet rs, StatementContext ctx) throws SQLException {
        return ArpPacketEntry.create(
                UUID.fromString(rs.getString("tap_uuid")),
                rs.getString("ethernet_source_mac"),
                rs.getString("ethernet_destination_mac"),
                rs.getString("hardware_type"),
                rs.getString("protocol_type"),
                rs.getString("operation"),
                rs.getString("arp_sender_mac"),
                rs.getString("arp_sender_address"),
                rs.getString("arp_target_mac"),
                rs.getString("arp_target_address"),
                rs.getInt("size"),
                new DateTime(rs.getTimestamp("timestamp"))
        );
    }

}
