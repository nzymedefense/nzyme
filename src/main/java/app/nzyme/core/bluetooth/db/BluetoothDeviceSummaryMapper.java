package app.nzyme.core.bluetooth.db;

import com.google.common.collect.Lists;
import org.jdbi.v3.core.mapper.RowMapper;
import org.jdbi.v3.core.statement.StatementContext;
import org.joda.time.DateTime;

import java.sql.ResultSet;
import java.sql.SQLException;

public class BluetoothDeviceSummaryMapper implements RowMapper<BluetoothDeviceSummary> {

    @Override
    public BluetoothDeviceSummary map(ResultSet rs, StatementContext ctx) throws SQLException {
        return BluetoothDeviceSummary.create(
                rs.getString("mac"),
                Lists.newArrayList((String[]) rs.getArray("aliases").getArray()),
                Lists.newArrayList((String[]) rs.getArray("devices").getArray()),
                Lists.newArrayList((String[]) rs.getArray("transports").getArray()),
                Lists.newArrayList((String[]) rs.getArray("names").getArray()),
                rs.getDouble("average_rssi"),
                Lists.newArrayList((Integer[]) rs.getArray("company_ids").getArray()),
                Lists.newArrayList((Integer[]) rs.getArray("class_numbers").getArray()),
                new DateTime(rs.getTimestamp("first_seen")),
                new DateTime(rs.getTimestamp("last_seen"))
        );
    }

}
