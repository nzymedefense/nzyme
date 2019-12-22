/*
 *  This file is part of nzyme.
 *
 *  nzyme is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  nzyme is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with nzyme.  If not, see <http://www.gnu.org/licenses/>.
 */

package horse.wtf.nzyme.alerts.service;

import horse.wtf.nzyme.Subsystem;
import horse.wtf.nzyme.alerts.Alert;
import horse.wtf.nzyme.database.Database;
import org.jdbi.v3.core.mapper.RowMapper;
import org.jdbi.v3.core.statement.StatementContext;
import org.joda.time.DateTime;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.UUID;

public class AlertDatabaseEntryMapper implements RowMapper<AlertDatabaseEntry> {

    @Override
    public AlertDatabaseEntry map(ResultSet rs, StatementContext ctx) throws SQLException {
        return AlertDatabaseEntry.create(
                rs.getLong("id"),
                UUID.fromString(rs.getString("alert_uuid")),
                Alert.TYPE.valueOf(rs.getString("alert_type")),
                Subsystem.valueOf(rs.getString("subsystem")),
                rs.getString("fields"),
                DateTime.parse(rs.getString("first_seen"), Database.DATE_TIME_FORMATTER),
                DateTime.parse(rs.getString("last_seen"), Database.DATE_TIME_FORMATTER),
                rs.getLong("frame_count")
        );
    }

}
