package app.nzyme.core.bluetooth.db;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.Lists;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jdbi.v3.core.mapper.RowMapper;
import org.jdbi.v3.core.statement.StatementContext;
import org.joda.time.DateTime;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

public class BluetoothDeviceSummaryMapper implements RowMapper<BluetoothDeviceSummary> {

    private static final Logger LOG = LogManager.getLogger(BluetoothDeviceSummaryMapper.class);

    private final ObjectMapper om;

    public BluetoothDeviceSummaryMapper() {
        this.om = new ObjectMapper()
                .configure(DeserializationFeature.FAIL_ON_IGNORED_PROPERTIES, false);
    }

    @Override
    public BluetoothDeviceSummary map(ResultSet rs, StatementContext ctx) throws SQLException {
        List<String> serviceUuids = Lists.newArrayList();

        // Parse service UUIDs JSON into list of service names.
        String[] serviceUuidsArray = (String[]) rs.getArray("service_uuids").getArray();
        if (serviceUuidsArray != null) {
            for (String x : serviceUuidsArray) {
                try {
                    List<BluetoothServiceUuidJson> json = this.om.readValue(x, new TypeReference<>() {});

                    for (BluetoothServiceUuidJson uuid : json) {
                        if (uuid.name() != null) {
                            serviceUuids.add(uuid.name());
                        }
                    }
                } catch(JsonProcessingException e) {
                    LOG.error("Skipping invalid service UUIDs payload of bluetooth device with MAC [{}].",
                            rs.getString("mac"), e);
                }
            }
        }

        return BluetoothDeviceSummary.create(
                rs.getString("mac"),
                Lists.newArrayList((String[]) rs.getArray("aliases").getArray()),
                Lists.newArrayList((String[]) rs.getArray("devices").getArray()),
                Lists.newArrayList((String[]) rs.getArray("transports").getArray()),
                Lists.newArrayList((String[]) rs.getArray("names").getArray()),
                rs.getDouble("average_rssi"),
                Lists.newArrayList((Integer[]) rs.getArray("company_ids").getArray()),
                Lists.newArrayList((Integer[]) rs.getArray("class_numbers").getArray()),
                serviceUuids,
                new DateTime(rs.getTimestamp("first_seen")),
                new DateTime(rs.getTimestamp("last_seen"))
        );
    }

}
