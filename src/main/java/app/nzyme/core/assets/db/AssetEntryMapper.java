package app.nzyme.core.assets.db;

import org.jdbi.v3.core.mapper.RowMapper;
import org.jdbi.v3.core.statement.StatementContext;
import org.joda.time.DateTime;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.UUID;

public class AssetEntryMapper implements RowMapper<AssetEntry> {
    @Override
    public AssetEntry map(ResultSet rs, StatementContext ctx) throws SQLException {
        return AssetEntry.create(
                rs.getLong("id"),
                UUID.fromString(rs.getString("uuid")),
                UUID.fromString(rs.getString("organization_id")),
                UUID.fromString(rs.getString("tenant_id")),
                rs.getString("mac"),
                rs.getString("dhcp_fingerprint_initial"),
                rs.getString("dhcp_fingerprint_renew"),
                rs.getString("dhcp_fingerprint_reboot"),
                rs.getString("dhcp_fingerprint_rebind"),
                rs.getBoolean("seen_dhcp"),
                rs.getBoolean("seen_tcp"),
                rs.getBoolean("seen_udp"),
                new DateTime(rs.getTimestamp("first_seen")),
                new DateTime(rs.getTimestamp("last_seen")),
                new DateTime(rs.getTimestamp("updated_at")),
                new DateTime(rs.getTimestamp("created_at"))
        );
    }
}
