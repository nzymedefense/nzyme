package app.nzyme.core.dot11.db;

import org.jdbi.v3.core.mapper.RowMapper;
import org.jdbi.v3.core.statement.StatementContext;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.UUID;

public class BSSIDWithTapMapper implements RowMapper<BSSIDWithTap> {

    @Override
    public BSSIDWithTap map(ResultSet rs, StatementContext ctx) throws SQLException {
        return BSSIDWithTap.create(
                rs.getString("bssid"),
                UUID.fromString(rs.getString("tap_uuid"))
        );
    }

}
