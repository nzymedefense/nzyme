package app.nzyme.core.dot11.db;

import com.google.common.collect.Lists;
import org.jdbi.v3.core.mapper.RowMapper;
import org.jdbi.v3.core.statement.StatementContext;
import org.joda.time.DateTime;

import java.sql.ResultSet;
import java.sql.SQLException;

public class SSIDDetailsMapper implements RowMapper<SSIDDetails> {

    @Override
    public SSIDDetails map(ResultSet rs, StatementContext ctx) throws SQLException {
        return SSIDDetails.create(
                rs.getString("ssid"),
                Lists.newArrayList((String[]) rs.getArray("security_protocols").getArray()),
                Lists.newArrayList((String[]) rs.getArray("fingerprints").getArray()),
                Lists.newArrayList((Double[]) rs.getArray("rates").getArray()),
                Lists.newArrayList((Boolean[]) rs.getArray("is_wps").getArray()),
                Lists.newArrayList((String[]) rs.getArray("infrastructure_types").getArray()),
                Lists.newArrayList((String[]) rs.getArray("security_suites").getArray()),
                rs.getFloat("signal_strength_average"),
                rs.getLong("total_bytes"),
                rs.getLong("total_frames"),
                new DateTime(rs.getTimestamp("last_seen"))
        );
    }
}
