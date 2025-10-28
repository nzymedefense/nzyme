package app.nzyme.core.ethernet.l4.db;

import app.nzyme.core.ethernet.L4MapperTools;
import app.nzyme.core.ethernet.L4Type;
import org.jdbi.v3.core.mapper.RowMapper;
import org.jdbi.v3.core.statement.StatementContext;
import org.joda.time.DateTime;

import java.sql.ResultSet;
import java.sql.SQLException;

public class L4SessionMapper implements RowMapper<L4Session> {

    @Override
    public L4Session map(ResultSet rs, StatementContext ctx) throws SQLException {
        return L4Session.create(
                rs.getString("session_key"),
                L4Type.valueOf(rs.getString("l4_type")),
                L4MapperTools.fieldsToAddressData("source", rs),
                L4MapperTools.fieldsToAddressData("destination", rs),
                rs.getLong("bytes_rx_count"),
                rs.getLong("bytes_tx_count"),
                rs.getLong("segments_count"),
                new DateTime(rs.getTimestamp("start_time")),
                rs.getTimestamp("end_time") == null ? null : new DateTime(rs.getTimestamp("end_time")),
                new DateTime(rs.getTimestamp("most_recent_segment_time")),
                rs.getString("state"),
                new DateTime(rs.getTimestamp("created_at"))
        );
    }

}
