package app.nzyme.core.detection.alerts.db;

import org.jdbi.v3.core.mapper.RowMapper;
import org.jdbi.v3.core.statement.StatementContext;
import org.joda.time.DateTime;

import java.sql.ResultSet;
import java.sql.SQLException;

public class DetectionAlertTimelineEntryMapper implements RowMapper<DetectionAlertTimelineEntry> {

    @Override
    public DetectionAlertTimelineEntry map(ResultSet rs, StatementContext ctx) throws SQLException {
        return DetectionAlertTimelineEntry.create(
                new DateTime(rs.getTimestamp("seen_from")),
                new DateTime(rs.getTimestamp("seen_to"))
        );
    }

}
