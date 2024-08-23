package app.nzyme.core.shared.db;

import org.jdbi.v3.core.mapper.RowMapper;
import org.jdbi.v3.core.statement.StatementContext;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.UUID;

public class TapBasedSignalStrengthResultMapper implements RowMapper<TapBasedSignalStrengthResult> {

    @Override
    public TapBasedSignalStrengthResult map(ResultSet rs, StatementContext ctx) throws SQLException {
        return TapBasedSignalStrengthResult.create(
                UUID.fromString(rs.getString("tap_uuid")),
                rs.getString("tap_name"),
                rs.getFloat("signal_strength")
        );
    }

}
