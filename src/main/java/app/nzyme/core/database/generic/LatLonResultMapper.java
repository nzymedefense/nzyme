package app.nzyme.core.database.generic;

import org.jdbi.v3.core.mapper.RowMapper;
import org.jdbi.v3.core.statement.StatementContext;

import java.sql.ResultSet;
import java.sql.SQLException;

public class LatLonResultMapper implements RowMapper<LatLonResult> {
    @Override
    public LatLonResult map(ResultSet rs, StatementContext ctx) throws SQLException {
        return LatLonResult.create(
                rs.getDouble("lat"),
                rs.getDouble("lon")
        );
    }
}
