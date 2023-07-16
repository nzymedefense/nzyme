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

public class DisconnectedClientDetailsMapper implements RowMapper<DisconnectedClientDetails> {

    @Override
    public DisconnectedClientDetails map(ResultSet rs, StatementContext ctx) throws SQLException {
        // Remove all NULL probe requests.
        List<String> probeRequests = Lists.newArrayList((String[]) rs.getArray("probe_requests").getArray())
                .parallelStream()
                .filter(Objects::nonNull)
                .collect(Collectors.toList());

        return DisconnectedClientDetails.create(
                rs.getString("client_mac"),
                new DateTime(rs.getTimestamp("last_seen")),
                probeRequests
        );
    }

}
