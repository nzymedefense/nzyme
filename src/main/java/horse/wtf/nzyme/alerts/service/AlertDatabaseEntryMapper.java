/*
 * This file is part of nzyme.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the Server Side Public License, version 1,
 * as published by MongoDB, Inc.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * Server Side Public License for more details.
 *
 * You should have received a copy of the Server Side Public License
 * along with this program. If not, see
 * <http://www.mongodb.com/licensing/server-side-public-license>.
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
                DateTime.parse(rs.getString("first_seen"), Database.DATABASE_DATE_TIME_FORMATTER),
                DateTime.parse(rs.getString("last_seen"), Database.DATABASE_DATE_TIME_FORMATTER),
                rs.getLong("frame_count")
        );
    }

}
