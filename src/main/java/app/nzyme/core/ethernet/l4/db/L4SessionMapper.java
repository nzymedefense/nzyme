package app.nzyme.core.ethernet.l4.db;

import app.nzyme.core.ethernet.L4MapperTools;
import app.nzyme.core.ethernet.L4Type;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jdbi.v3.core.mapper.RowMapper;
import org.jdbi.v3.core.statement.StatementContext;
import org.joda.time.DateTime;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

public class L4SessionMapper implements RowMapper<L4Session> {

    private static final Logger LOG = LogManager.getLogger(L4SessionMapper.class);

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public L4Session map(ResultSet rs, StatementContext ctx) throws SQLException {
        List<String> tags;
        String tagsS = rs.getString("tags");
        if (tagsS != null) {
            try {
                tags = objectMapper.readValue(tagsS, new TypeReference<>() {});
            } catch (JsonProcessingException e) {
                LOG.error("Could not parse L4 session tags.", e);
                tags = null;
            }
        } else {
            tags = null;
        }

        return L4Session.create(
                rs.getString("session_key"),
                L4Type.valueOf(rs.getString("l4_type")),
                L4MapperTools.fieldsToAddressData("source", rs),
                L4MapperTools.fieldsToAddressData("destination", rs),
                rs.getLong("bytes_rx_count"),
                rs.getLong("bytes_tx_count"),
                rs.getLong("segments_count"),
                new DateTime(rs.getTimestamp("start_time")),
                rs.getTimestamp("end_time") == null ? null : new DateTime(rs.getTimestamp("end_time")),
                new DateTime(rs.getTimestamp("most_recent_segment_time")),
                rs.getLong("duration_ms"),
                rs.getString("state"),
                rs.getString("fingerprint"),
                tags,
                new DateTime(rs.getTimestamp("created_at"))
        );
    }

}
