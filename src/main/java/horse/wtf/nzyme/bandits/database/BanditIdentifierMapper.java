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

package horse.wtf.nzyme.bandits.database;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import horse.wtf.nzyme.bandits.identifiers.*;
import horse.wtf.nzyme.notifications.FieldNames;
import org.jdbi.v3.core.mapper.RowMapper;
import org.jdbi.v3.core.statement.StatementContext;

import java.io.IOException;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Map;

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
            return BanditIdentifierFactory.create(type, config);
        } catch (BanditIdentifierFactory.NoSerializerException e) {
            throw new SQLException("No serializer configured for bandit identifier of type [" + type + "].");
        } catch (BanditIdentifierFactory.MappingException e) {
            throw new SQLException("Could not map configuration to bandit identifier..");
        }
    }

}

