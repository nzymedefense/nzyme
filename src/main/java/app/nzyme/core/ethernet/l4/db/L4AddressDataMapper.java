package app.nzyme.core.ethernet.l4.db;

import app.nzyme.core.ethernet.GeoData;
import org.jdbi.v3.core.mapper.RowMapper;
import org.jdbi.v3.core.statement.StatementContext;

import java.sql.ResultSet;
import java.sql.SQLException;

public class L4AddressDataMapper implements RowMapper<L4AddressData> {

    @Override
    public L4AddressData map(ResultSet rs, StatementContext ctx) throws SQLException {
        return L4AddressData.create(
                rs.getString("mac"),
                rs.getString("address"),
                rs.getInt("port"),
                GeoData.create(
                        rs.getInt("geo_asn_number"),
                        rs.getString("geo_asn_name"),
                        rs.getString("geo_asn_domain"),
                        rs.getString("geo_city"),
                        rs.getString("geo_country_code"),
                        rs.getFloat("geo_latitude"),
                        rs.getFloat("geo_longitude")
                ),
                L4AddressAttributes.create(
                        rs.getBoolean("is_site_local"),
                        rs.getBoolean("is_loopback"),
                        rs.getBoolean("is_multicast")
                )
        );
    }

}
