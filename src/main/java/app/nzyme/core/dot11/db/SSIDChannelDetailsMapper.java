package app.nzyme.core.dot11.db;

import com.google.common.collect.Lists;
import org.jdbi.v3.core.mapper.RowMapper;
import org.jdbi.v3.core.statement.StatementContext;
import org.joda.time.DateTime;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

public class SSIDChannelDetailsMapper implements RowMapper<SSIDChannelDetails> {

    @Override
    public SSIDChannelDetails map(ResultSet rs, StatementContext ctx) throws SQLException {
        // The is_wps flag is stored as String for database simplicity reasons and we have to cast here.
        List<Boolean> isWps = Lists.newArrayList();
        for (String wps : ((String[]) rs.getArray("is_wps").getArray())) {
            isWps.add(Boolean.valueOf(wps));
        }

        return SSIDChannelDetails.create(
                rs.getString("ssid"),
                Lists.newArrayList((String[]) rs.getArray("security_protocols").getArray()),
                isWps,
                Lists.newArrayList((String[]) rs.getArray("infrastructure_types").getArray()),
                rs.getFloat("signal_strength_average"),
                rs.getInt("frequency"),
                rs.getLong("total_bytes"),
                rs.getLong("total_frames"),
                new DateTime(rs.getTimestamp("last_seen"))
        );
    }

}
