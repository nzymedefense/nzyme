package app.nzyme.core.ethernet.recon.db;

import com.google.common.collect.Lists;
import org.jdbi.v3.core.mapper.RowMapper;
import org.jdbi.v3.core.statement.StatementContext;
import org.joda.time.DateTime;
import tools.jackson.core.type.TypeReference;
import tools.jackson.databind.ObjectMapper;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.UUID;

public class ReconScanEntryMapper implements RowMapper<ReconScanEntry> {

    private static final ObjectMapper OM = new ObjectMapper();

    @Override
    public ReconScanEntry map(ResultSet rs, StatementContext ctx) throws SQLException {
        List<UUID> taps = Lists.newArrayList();
        String tapsJson = rs.getString("tap_uuids");
        if (tapsJson != null && !tapsJson.isBlank()) {
            try {
                List<String> parsed = OM.readValue(tapsJson, new TypeReference<>() {});
                for (String t : parsed) {
                    taps.add(UUID.fromString(t));
                }
            } catch (Exception e) {
                throw new SQLException("Could not parse tap UUIDs of recon scan.", e);
            }
        }

        return ReconScanEntry.create(
                rs.getLong("id"),
                UUID.fromString(rs.getString("uuid")),
                UUID.fromString(rs.getString("organization_id")),
                UUID.fromString(rs.getString("tenant_id")),
                rs.getString("type"),
                rs.getString("source_address"),
                rs.getString("source_mac"),
                rs.getString("target_address"),
                rs.getInt("host_count"),
                rs.getInt("port_count"),
                rs.getLong("probe_count"),
                rs.getLong("open_count"),
                rs.getLong("closed_count"),
                rs.getLong("unanswered_count"),
                rs.getBoolean("targets_truncated"),
                rs.getString("enrichment"),
                taps,
                new DateTime(rs.getTimestamp("first_seen")),
                new DateTime(rs.getTimestamp("last_seen")),
                new DateTime(rs.getTimestamp("created_at")),
                new DateTime(rs.getTimestamp("updated_at"))
        );
    }

}
