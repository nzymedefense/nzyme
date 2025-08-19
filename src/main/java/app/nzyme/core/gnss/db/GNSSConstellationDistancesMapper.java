package app.nzyme.core.gnss.db;

import org.jdbi.v3.core.mapper.RowMapper;
import org.jdbi.v3.core.statement.StatementContext;

import java.sql.ResultSet;
import java.sql.SQLException;

public class GNSSConstellationDistancesMapper implements RowMapper<GNSSConstellationDistances> {

    @Override
    public GNSSConstellationDistances map(ResultSet rs, StatementContext ctx) throws SQLException {
        return GNSSConstellationDistances.create(
                rs.getObject("gps", Double.class),
                rs.getObject("glonass", Double.class),
                rs.getObject("beidou", Double.class),
                rs.getObject("galileo", Double.class)
        );
    }

}
