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

import app.nzyme.core.Role;
import app.nzyme.core.bandits.Contact;
import app.nzyme.core.database.DatabaseImpl;
import org.jdbi.v3.core.mapper.RowMapper;
import org.jdbi.v3.core.statement.StatementContext;
import org.joda.time.DateTime;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.UUID;

public class ContactMapper implements RowMapper<Contact> {

    @Override
    public Contact map(ResultSet rs, StatementContext ctx) throws SQLException {
        return Contact.create(
                UUID.fromString(rs.getString("contact_uuid")),
                DateTime.parse(rs.getString("first_seen"), DatabaseImpl.DATABASE_DATE_TIME_FORMATTER),
                DateTime.parse(rs.getString("last_seen"), DatabaseImpl.DATABASE_DATE_TIME_FORMATTER),
                rs.getLong("frame_count"),
                Role.valueOf(rs.getString("source_role")),
                rs.getString("source_name"),
                rs.getInt("last_signal"),
                rs.getLong("bandit_id"),
                null
        );
    }

}
