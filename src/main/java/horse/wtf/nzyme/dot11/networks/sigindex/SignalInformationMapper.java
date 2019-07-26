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

package horse.wtf.nzyme.dot11.networks.sigindex;

import horse.wtf.nzyme.database.Database;
import org.jdbi.v3.core.mapper.RowMapper;
import org.jdbi.v3.core.statement.StatementContext;
import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;

import java.sql.ResultSet;
import java.sql.SQLException;

public class SignalInformationMapper implements RowMapper<SignalInformation> {

    @Override
    public SignalInformation map(ResultSet rs, StatementContext ctx) throws SQLException {
        return SignalInformation.create(
                rs.getInt("channel"),
                DateTime.parse(rs.getString("bucket"), Database.BUCKET_DATE_TIME_FORMATTER),
                rs.getFloat("avg_signal_index"),
                rs.getFloat("avg_signal_index_threshold"),
                rs.getFloat("avg_signal_quality"),
                rs.getFloat("avg_signal_stddev"),
                rs.getFloat("avg_expected_delta_lower"),
                rs.getFloat("avg_expected_delta_upper")
        );
    }

}
