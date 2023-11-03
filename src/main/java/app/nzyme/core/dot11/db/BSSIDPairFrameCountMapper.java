package app.nzyme.core.dot11.db;

import org.jdbi.v3.core.mapper.RowMapper;
import org.jdbi.v3.core.statement.StatementContext;

import java.sql.ResultSet;
import java.sql.SQLException;

public class BSSIDPairFrameCountMapper implements RowMapper<BSSIDPairFrameCount> {

    @Override
    public BSSIDPairFrameCount map(ResultSet rs, StatementContext ctx) throws SQLException {
        return BSSIDPairFrameCount.create(
                rs.getString("sender"),
                rs.getString("receiver"),
                rs.getLong("frame_count")
        );
    }

}
