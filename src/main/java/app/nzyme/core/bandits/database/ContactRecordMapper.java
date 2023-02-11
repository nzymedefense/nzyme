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

package app.nzyme.core.bandits.database;

import app.nzyme.core.bandits.engine.ContactRecord;
import app.nzyme.core.bandits.engine.ContactRecorder;
import org.jdbi.v3.core.mapper.RowMapper;
import org.jdbi.v3.core.statement.StatementContext;
import org.joda.time.DateTime;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.UUID;

public class ContactRecordMapper implements RowMapper<ContactRecord>  {

    @Override
    public ContactRecord map(ResultSet rs, StatementContext ctx) throws SQLException {
        ContactRecorder.RECORD_TYPE recordType;
        try {
            recordType = ContactRecorder.RECORD_TYPE.valueOf(rs.getString("record_type"));
        } catch (IllegalArgumentException e) {
            throw new SQLException("Cannot serialize contact record of unknown type.", e);
        }

        return ContactRecord.create(
                UUID.fromString(rs.getString("contact_uuid")),
                recordType,
                rs.getString("record_value"),
                rs.getLong("frame_count"),
                rs.getDouble("rssi_average"),
                rs.getDouble("rssi_stddev"),
                new DateTime(rs.getTimestamp("created_at"))
        );
    }

}
