package app.nzyme.core.ethernet.recon.db;

import org.jdbi.v3.core.mapper.RowMapper;
import org.jdbi.v3.core.statement.StatementContext;
import org.joda.time.DateTime;

import java.sql.ResultSet;
import java.sql.SQLException;

public class ReconScanTargetEntryMapper implements RowMapper<ReconScanTargetEntry> {

    @Override
    public ReconScanTargetEntry map(ResultSet rs, StatementContext ctx) throws SQLException {
        int port = rs.getInt("target_port");
        boolean portNull = rs.wasNull();

        return ReconScanTargetEntry.create(
                rs.getLong("id"),
                rs.getLong("scan_id"),
                rs.getString("target_address"),
                portNull ? null : port,
                rs.getString("outcome"),
                rs.getLong("probe_count"),
                new DateTime(rs.getTimestamp("first_seen")),
                new DateTime(rs.getTimestamp("last_seen"))
        );
    }

}
