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

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import app.nzyme.core.bandits.identifiers.*;
import app.nzyme.core.notifications.FieldNames;
import org.jdbi.v3.core.mapper.RowMapper;
import org.jdbi.v3.core.statement.StatementContext;

import java.io.IOException;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Map;
import java.util.UUID;

public class BanditIdentifierMapper implements RowMapper<BanditIdentifier> {

    private final ObjectMapper om;

    public BanditIdentifierMapper() {
        this.om = new ObjectMapper();
    }

    @Override
    public BanditIdentifier map(ResultSet rs, StatementContext ctx) throws SQLException {
        BanditIdentifier.TYPE type;
        try {
            type = BanditIdentifier.TYPE.valueOf(rs.getString("identifier_type"));
        } catch (IllegalArgumentException e) {
            throw new SQLException("Cannot serialize bandit identifier of unknown type.", e);
        }

        Map<String, Object> config;
        try {
            config = om.readValue(rs.getString("configuration"), new TypeReference<Map<String, Object>>(){});
        } catch (IOException e) {
            throw new SQLException("Cannot serialize bandit identifier configuration.", e);
        }

        try {
            return BanditIdentifierFactory.create(type, config, rs.getLong(FieldNames.ID), UUID.fromString(rs.getString(FieldNames.IDENTIFIER_UUID)));
        } catch (BanditIdentifierFactory.NoSerializerException e) {
            throw new SQLException("No serializer configured for bandit identifier of type [" + type + "].");
        } catch (BanditIdentifierFactory.MappingException e) {
            throw new SQLException("Could not map configuration to bandit identifier..");
        }
    }

}

