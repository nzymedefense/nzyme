package app.nzyme.core.uav.db;

import org.jdbi.v3.core.mapper.RowMapper;
import org.jdbi.v3.core.statement.StatementContext;
import org.joda.time.DateTime;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.UUID;

public class UavTimelineEntryMapper implements RowMapper<UavTimelineEntry> {

    @Override
    public UavTimelineEntry map(ResultSet rs, StatementContext ctx) throws SQLException {
        return UavTimelineEntry.create(
                rs.getLong("id"),
                UUID.fromString(rs.getString("uuid")),
                new DateTime(rs.getTimestamp("seen_from")),
                new DateTime(rs.getTimestamp("seen_to"))
        );
    }

}
