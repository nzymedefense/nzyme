package app.nzyme.core.ethernet.dhcp.db;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.joda.JodaModule;
import org.jdbi.v3.core.mapper.RowMapper;
import org.jdbi.v3.core.statement.StatementContext;
import org.joda.time.DateTime;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class DHCPTransactionMapper implements RowMapper<DHCPTransaction> {

    private final ObjectMapper om = new ObjectMapper()
            .registerModule(new JodaModule())
            .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);

    @Override
    public DHCPTransaction map(ResultSet rs, StatementContext ctx) throws SQLException {
        DateTime firstPacket = rs.getTimestamp("first_packet") == null ?
                null : new DateTime(rs.getTimestamp("first_packet").getTime());

        List<String> additionalClientMacs;
        List<String> additionalServerMacs;
        List<String> offeredIpAddresses;
        List<String> additionalOptionsFingerprints;
        List<String> notes;
        Map<String, List<DateTime>> timestamps;

        try {
            additionalClientMacs = jsonbStringList(rs, "additional_client_macs");
            additionalServerMacs = jsonbStringList(rs, "additional_server_macs");
            offeredIpAddresses = jsonbStringList(rs, "offered_ip_addresses");
            additionalOptionsFingerprints = jsonbStringList(rs, "additional_options_fingerprints");
            notes = jsonbStringList(rs, "notes");

            timestamps = om.readValue(rs.getString("timestamps"), new TypeReference<>() {});
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Failed to parse JSON from DHCP transaction database object.", e);
        }

        return DHCPTransaction.create(
                rs.getLong("transaction_id"),
                rs.getString("transaction_type"),
                rs.getString("client_mac"),
                additionalClientMacs,
                rs.getString("server_mac"),
                additionalServerMacs,
                offeredIpAddresses,
                rs.getString("requested_ip_address"),
                rs.getString("options_fingerprint"),
                additionalOptionsFingerprints,
                timestamps,
                firstPacket,
                new DateTime(rs.getTimestamp("latest_packet")),
                notes,
                rs.getBoolean("is_complete")
        );
    }

    private List<String> jsonbStringList(ResultSet rs, String columnName) throws SQLException, JsonProcessingException {
        return om.readValue(rs.getString(columnName), new TypeReference<ArrayList<String>>() {});
    }

}
