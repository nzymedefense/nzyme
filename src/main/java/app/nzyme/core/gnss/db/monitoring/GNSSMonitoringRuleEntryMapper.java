package app.nzyme.core.gnss.db.monitoring;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.jdbi.v3.core.mapper.RowMapper;
import org.jdbi.v3.core.statement.StatementContext;
import org.joda.time.DateTime;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

public class GNSSMonitoringRuleEntryMapper implements RowMapper<GNSSMonitoringRuleEntry> {

    private final ObjectMapper om;

    public GNSSMonitoringRuleEntryMapper() {
        this.om = new ObjectMapper();
    }

    @Override
    public GNSSMonitoringRuleEntry map(ResultSet rs, StatementContext ctx) throws SQLException {
        Map<String, List<Object>> conditions;
        Optional<List<UUID>> taps;
        try {
            conditions = om.readValue(rs.getString("conditions"), new TypeReference<>() {});

            if (rs.getString("taps") == null) {
                taps = Optional.empty();
            } else {
                taps = Optional.of(om.readValue(rs.getString("taps"), new TypeReference<>() {}));
            }
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }

        return GNSSMonitoringRuleEntry.create(
                rs.getLong("id"),
                UUID.fromString(rs.getString("uuid")),
                UUID.fromString(rs.getString("organization_id")),
                UUID.fromString(rs.getString("tenant_id")),
                rs.getString("name"),
                rs.getString("description"),
                conditions,
                taps,
                new DateTime(rs.getTimestamp("updated_at")),
                new DateTime(rs.getTimestamp("created_at"))
        );
    }

}
