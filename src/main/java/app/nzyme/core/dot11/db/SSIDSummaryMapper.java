package app.nzyme.core.dot11.db;

import com.google.common.collect.Lists;
import org.jdbi.v3.core.mapper.RowMapper;
import org.jdbi.v3.core.statement.StatementContext;
import org.joda.time.DateTime;

import java.sql.ResultSet;
import java.sql.SQLException;

public class SSIDSummaryMapper implements RowMapper<SSIDSummary> {

    @Override
    public SSIDSummary map(ResultSet rs, StatementContext ctx) throws SQLException {
        return SSIDSummary.create(
                rs.getString("ssid"),
                Lists.newArrayList((String[]) rs.getArray("security_protocols").getArray()),
                Lists.newArrayList((Boolean[]) rs.getArray("is_wps").getArray()),
                Lists.newArrayList((String[]) rs.getArray("infrastructure_types").getArray()),
                rs.getFloat("signal_strength_average"),
                rs.getInt("frequency"),
                rs.getLong("total_bytes"),
                rs.getLong("total_frames"),
                new DateTime(rs.getTimestamp("last_seen"))
        );
    }

}
