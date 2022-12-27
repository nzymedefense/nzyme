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

package app.nzyme.core.reporting.db;

import org.jdbi.v3.core.mapper.RowMapper;
import org.jdbi.v3.core.statement.StatementContext;
import org.joda.time.DateTime;

import java.sql.ResultSet;
import java.sql.SQLException;

public class ExecutionLogEntryMapper implements RowMapper<ExecutionLogEntry> {

    @Override
    public ExecutionLogEntry map(ResultSet rs, StatementContext ctx) throws SQLException {
        return ExecutionLogEntry.create(
                rs.getLong("id"),
                rs.getString("report_name"),
                rs.getString("result"),
                rs.getString("message"),
                rs.getString("content"),
                new DateTime(rs.getTimestamp("created_at"))
        );
    }

}
