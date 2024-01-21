package app.nzyme.core.dot11.db;

import org.jdbi.v3.core.mapper.RowMapper;
import org.jdbi.v3.core.statement.StatementContext;
import org.joda.time.DateTime;

import java.sql.ResultSet;
import java.sql.SQLException;

public class ClientSignalStrengthResultMapper implements RowMapper<ClientSignalStrengthResult> {

    @Override
    public ClientSignalStrengthResult map(ResultSet rs, StatementContext ctx) throws SQLException {
        return ClientSignalStrengthResult.create(
                new DateTime(rs.getTimestamp("bucket")),
                rs.getDouble("signal_strength")
        );
    }

}
