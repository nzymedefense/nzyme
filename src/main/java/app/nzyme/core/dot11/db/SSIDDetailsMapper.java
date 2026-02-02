package app.nzyme.core.dot11.db;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.Lists;
import org.jdbi.v3.core.mapper.RowMapper;
import org.jdbi.v3.core.statement.StatementContext;
import org.joda.time.DateTime;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Collections;
import java.util.List;

public class SSIDDetailsMapper implements RowMapper<SSIDDetails> {

    private static final ObjectMapper om = new ObjectMapper();

    @Override
    public SSIDDetails map(ResultSet rs, StatementContext ctx) throws SQLException {
        // The is_wps flag is stored as String for database simplicity reasons and we have to cast here.
        List<Boolean> hasWps = Lists.newArrayList();
        Collections.addAll(hasWps, ((Boolean[]) rs.getArray("has_wps").getArray()));


        List<Dot11SecuritySuiteJson> securitySuites =
                SecuritySettingsParser.parseSecuritySettings(rs.getArray("security_settings"));

        return SSIDDetails.create(
                rs.getString("ssid"),
                Lists.newArrayList((String[]) rs.getArray("security_protocols").getArray()),
                Lists.newArrayList((String[]) rs.getArray("fingerprints").getArray()),
                Lists.newArrayList((Integer[]) rs.getArray("frequencies").getArray()),
                Lists.newArrayList((Double[]) rs.getArray("rates").getArray()),
                hasWps,
                Lists.newArrayList((String[]) rs.getArray("infrastructure_types").getArray()),
                securitySuites,
                Lists.newArrayList((String[]) rs.getArray("access_point_clients").getArray()),
                rs.getFloat("signal_strength_average"),
                rs.getLong("total_bytes"),
                rs.getLong("total_frames"),
                new DateTime(rs.getTimestamp("last_seen"))
        );
    }

}
