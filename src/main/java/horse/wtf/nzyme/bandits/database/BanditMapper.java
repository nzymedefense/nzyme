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

import com.fasterxml.jackson.databind.ObjectMapper;
import horse.wtf.nzyme.bandits.Bandit;
import horse.wtf.nzyme.database.Database;
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
                DateTime.parse(rs.getString("created_at"), Database.DATE_TIME_FORMATTER),
                DateTime.parse(rs.getString("updated_at"), Database.DATE_TIME_FORMATTER),
                null
        );
    }

}
