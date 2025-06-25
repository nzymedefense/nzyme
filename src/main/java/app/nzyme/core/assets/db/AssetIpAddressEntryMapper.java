package app.nzyme.core.assets.db;

import org.jdbi.v3.core.mapper.RowMapper;
import org.jdbi.v3.core.statement.StatementContext;
import org.joda.time.DateTime;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.UUID;

public class AssetIpAddressEntryMapper implements RowMapper<AssetIpAddressEntry> {

    @Override
    public AssetIpAddressEntry map(ResultSet rs, StatementContext ctx) throws SQLException {
        return AssetIpAddressEntry.create(
                rs.getLong("id"),
                rs.getLong("asset_id"),
                UUID.fromString(rs.getString("uuid")),
                rs.getString("address"),
                rs.getString("source"),
                new DateTime(rs.getTimestamp("first_seen")),
                new DateTime(rs.getTimestamp("last_seen"))
        );
    }

}
