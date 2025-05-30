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
import java.util.List;
import java.util.Map;

public class DHCPTransactionMapper implements RowMapper<DHCPTransactionEntry> {

    private final ObjectMapper om = new ObjectMapper()
            .registerModule(new JodaModule())
            .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);

    @Override
    public DHCPTransactionEntry map(ResultSet rs, StatementContext ctx) throws SQLException {
        DateTime firstPacket = rs.getTimestamp("first_packet") == null ?
                null : new DateTime(rs.getTimestamp("first_packet").getTime());

        List<String> additionalClientMacs;
        List<String> additionalServerMacs;
        List<String> offeredIpAddresses;
        List<Integer> options;
        List<List<Integer>> additionalOptions;
        List<String> additionalFingerprints;
        List<String> additionalVendorClasses;
        List<String> notes;
        Map<String, List<String>> timestamps;

        try {
            additionalClientMacs = om.readValue(rs.getString("additional_client_macs"), new TypeReference<>() {});
            additionalServerMacs = om.readValue(rs.getString("additional_server_macs"), new TypeReference<>() {});
            offeredIpAddresses = om.readValue(rs.getString("offered_ip_addresses"), new TypeReference<>() {});
            options = om.readValue(rs.getString("options"), new TypeReference<>() {});
            additionalOptions = om.readValue(rs.getString("additional_options"), new TypeReference<>() {});
            additionalFingerprints = om.readValue(rs.getString("additional_fingerprints"), new TypeReference<>() {});
            additionalVendorClasses = om.readValue(rs.getString("additional_vendor_classes"), new TypeReference<>() {});
            notes = om.readValue(rs.getString("notes"), new TypeReference<>() {});
            timestamps = om.readValue(rs.getString("timestamps"), new TypeReference<>() {});
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Failed to parse JSON from DHCP transaction database object.", e);
        }

        return DHCPTransactionEntry.create(
                rs.getLong("transaction_id"),
                rs.getString("transaction_type"),
                rs.getString("client_mac"),
                additionalClientMacs,
                rs.getString("server_mac"),
                additionalServerMacs,
                offeredIpAddresses,
                rs.getString("requested_ip_address"),
                options,
                additionalOptions,
                rs.getString("fingerprint"),
                additionalFingerprints,
                rs.getString("vendor_class"),
                additionalVendorClasses,
                timestamps,
                firstPacket,
                new DateTime(rs.getTimestamp("latest_packet")),
                notes,
                rs.getBoolean("is_successful"),
                rs.getBoolean("is_complete")
        );
    }


}
