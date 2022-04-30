package horse.wtf.nzyme.taps.db;

import horse.wtf.nzyme.taps.Tap;
import horse.wtf.nzyme.taps.TotalWithAverage;
import org.jdbi.v3.core.mapper.RowMapper;
import org.jdbi.v3.core.statement.StatementContext;
import org.joda.time.DateTime;

import java.sql.ResultSet;
import java.sql.SQLException;

public class TapMapper implements RowMapper<Tap>  {

    @Override
    public Tap map(ResultSet rs, StatementContext ctx) throws SQLException {
        return Tap.create(
                rs.getString("name"),
                new DateTime(rs.getTimestamp("local_time")),
                TotalWithAverage.create(rs.getLong("processed_bytes_total"), rs.getLong("processed_bytes_average")),
                rs.getLong("memory_total"),
                rs.getLong("memory_free"),
                rs.getLong("memory_used"),
                rs.getDouble("cpu_load"),
                new DateTime(rs.getTimestamp("created_at")),
                new DateTime(rs.getTimestamp("updated_at"))
        );
    }

}
