package app.nzyme.core.crypto.database;

import org.jdbi.v3.core.mapper.RowMapper;
import org.jdbi.v3.core.statement.StatementContext;
import org.joda.time.DateTime;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.UUID;

public class TLSKeyAndCertificateEntryMapper implements RowMapper<TLSKeyAndCertificateEntry> {

    @Override
    public TLSKeyAndCertificateEntry map(ResultSet rs, StatementContext ctx) throws SQLException {
        return TLSKeyAndCertificateEntry.create(
                UUID.fromString(rs.getString("node_id")),
                rs.getString("certificate"),
                rs.getString("key"),
                new DateTime(rs.getTimestamp("valid_from")),
                new DateTime(rs.getTimestamp("expires_at"))
        );
    }

}
