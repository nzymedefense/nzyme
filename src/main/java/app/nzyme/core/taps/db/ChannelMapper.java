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

package app.nzyme.core.taps.db;

import app.nzyme.core.taps.Channel;
import app.nzyme.core.taps.TotalWithAverage;
import org.jdbi.v3.core.mapper.RowMapper;
import org.jdbi.v3.core.statement.StatementContext;
import org.joda.time.DateTime;

import java.sql.ResultSet;
import java.sql.SQLException;

public class ChannelMapper implements RowMapper<Channel> {

    @Override
    public Channel map(ResultSet rs, StatementContext ctx) throws SQLException {
        return Channel.create(
            rs.getLong("bus_id"),
                rs.getString("name"),
                rs.getLong("capacity"),
                rs.getLong("watermark"),
                TotalWithAverage.create(
                        rs.getLong("errors_total"),
                        rs.getLong("errors_average")
                ),
                TotalWithAverage.create(
                        rs.getLong("throughput_bytes_total"),
                        rs.getLong("throughput_bytes_average")
                ),
                TotalWithAverage.create(
                        rs.getLong("throughput_messages_total"),
                        rs.getLong("throughput_messages_average")
                ),
                new DateTime(rs.getTimestamp("created_at")),
                new DateTime(rs.getTimestamp("updated_at"))
        );
    }

}
