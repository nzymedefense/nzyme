package app.nzyme.core.detection.alerts.db;

import org.jdbi.v3.core.mapper.RowMapper;
import org.jdbi.v3.core.statement.StatementContext;

import java.sql.ResultSet;
import java.sql.SQLException;

public class DetectionAlertAttributeEntryMapper implements RowMapper<DetectionAlertAttributeEntry> {

    @Override
    public DetectionAlertAttributeEntry map(ResultSet rs, StatementContext ctx) throws SQLException {
        return DetectionAlertAttributeEntry.create(
                rs.getLong("id"),
                rs.getLong("detection_alert_id"),
                rs.getString("attribute_key"),
                rs.getString("attribute_value")
        );
    }

}
