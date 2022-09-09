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

package horse.wtf.nzyme.bandits.database;

import com.fasterxml.jackson.databind.ObjectMapper;
import horse.wtf.nzyme.bandits.Bandit;
import horse.wtf.nzyme.database.DatabaseImpl;
import org.jdbi.v3.core.mapper.RowMapper;
import org.jdbi.v3.core.statement.StatementContext;
import org.joda.time.DateTime;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.UUID;

public class BanditMapper implements RowMapper<Bandit> {

    private final ObjectMapper om;

    public BanditMapper() {
        this.om = new ObjectMapper();
    }

    @Override
    public Bandit map(ResultSet rs, StatementContext ctx) throws SQLException {
        return Bandit.create(
                rs.getLong("id"),
                UUID.fromString(rs.getString("bandit_uuid")),
                rs.getString("name"),
                rs.getString("description"),
                rs.getBoolean("read_only"),
                DateTime.parse(rs.getString("created_at"), DatabaseImpl.DATABASE_DATE_TIME_FORMATTER),
                DateTime.parse(rs.getString("updated_at"), DatabaseImpl.DATABASE_DATE_TIME_FORMATTER),
                null
        );
    }

}
