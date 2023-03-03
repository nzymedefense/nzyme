package app.nzyme.core.crypto.database;

import app.nzyme.core.crypto.tls.TLSSourceType;
import org.jdbi.v3.core.mapper.RowMapper;
import org.jdbi.v3.core.statement.StatementContext;
import org.joda.time.DateTime;

import java.sql.ResultSet;
import java.sql.SQLException;

public class TLSWildcardKeyAndCertificateEntryMapper implements RowMapper<TLSWildcardKeyAndCertificateEntry> {

    @Override
    public TLSWildcardKeyAndCertificateEntry map(ResultSet rs, StatementContext ctx) throws SQLException {
        return TLSWildcardKeyAndCertificateEntry.create(
                rs.getLong("id"),
                rs.getString("node_matcher"),
                rs.getString("certificate"),
                rs.getString("key"),
                TLSSourceType.valueOf(rs.getString("source_type")),
                new DateTime(rs.getTimestamp("valid_from")),
                new DateTime(rs.getTimestamp("expires_at"))
        );
    }

}
