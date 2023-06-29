package app.nzyme.core.dot11.db;

import com.google.common.collect.Lists;
import org.jdbi.v3.core.mapper.RowMapper;
import org.jdbi.v3.core.statement.StatementContext;
import org.joda.time.DateTime;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

public class BSSIDSummaryMapper implements RowMapper<BSSIDSummary> {

    @Override
    public BSSIDSummary map(ResultSet rs, StatementContext ctx) throws SQLException {
        // Remove all NULL SSIDs.
        List<String> ssids = Lists.newArrayList((String[]) rs.getArray("ssids").getArray())
                .parallelStream()
                .filter(Objects::nonNull)
                .collect(Collectors.toList());

        return BSSIDSummary.create(
                rs.getString("bssid"),
                rs.getFloat("signal_strength_average"),
                new DateTime(rs.getTimestamp("last_seen")),
                rs.getLong("hidden_ssid_frames"),
                ssids,
                Lists.newArrayList((String[]) rs.getArray("security_protocols").getArray()),
                Lists.newArrayList((String[]) rs.getArray("fingerprints").getArray()),
                Lists.newArrayList((String[]) rs.getArray("infrastructure_types").getArray())
        );
    }

}
