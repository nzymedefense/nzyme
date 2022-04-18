package horse.wtf.nzyme.configuration.db;

import org.jdbi.v3.core.mapper.RowMapper;
import org.jdbi.v3.core.statement.StatementContext;
import org.joda.time.DateTime;

import java.sql.ResultSet;
import java.sql.SQLException;

public class BaseConfigurationMapper implements RowMapper<BaseConfiguration> {

    @Override
    public BaseConfiguration map(ResultSet rs, StatementContext ctx) throws SQLException {
        return BaseConfiguration.create(
                rs.getString("tap_secret"),
                new DateTime(rs.getTimestamp("updated_at"))
        );
    }

}
