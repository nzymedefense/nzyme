package app.nzyme.core.ethernet.tls.db;

import org.jdbi.v3.core.mapper.RowMapper;
import org.jdbi.v3.core.statement.StatementContext;
import org.joda.time.DateTime;

import java.sql.Array;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

public class TLSSessionTapEntryMapper implements RowMapper<TLSSessionTapEntry> {

    @Override
    public TLSSessionTapEntry map(ResultSet rs, StatementContext ctx) throws SQLException {
        return TLSSessionTapEntry.builder()
                .tapUuid(UUID.fromString(rs.getString("tap_uuid")))
                .clientHello(rs.getString("client_hello"))
                .serverHello(rs.getString("server_hello"))
                .alerts(rs.getString("alerts"))
                .serverChainSha256(stringList(rs.getArray("server_chain_sha256")))
                .clientChainSha256(stringList(rs.getArray("client_chain_sha256")))
                .clientChangeCipherSpec(rs.getBoolean("client_change_cipher_spec"))
                .serverChangeCipherSpec(rs.getBoolean("server_change_cipher_spec"))
                .clientAppDataRecords(rs.getLong("client_app_data_records"))
                .clientAppDataBytes(rs.getLong("client_app_data_bytes"))
                .serverAppDataRecords(rs.getLong("server_app_data_records"))
                .serverAppDataBytes(rs.getLong("server_app_data_bytes"))
                .mostRecentSegmentTime(new DateTime(rs.getTimestamp("most_recent_segment_time")))
                .build();
    }

    static List<String> stringList(Array array) throws SQLException {
        if (array == null) {
            return List.of();
        }

        return Arrays.asList((String[]) array.getArray());
    }

}
