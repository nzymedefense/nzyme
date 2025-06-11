package app.nzyme.core.assets;

import app.nzyme.core.NzymeNode;
import app.nzyme.core.assets.db.AssetEntry;
import app.nzyme.core.database.OrderDirection;
import app.nzyme.core.detection.alerts.DetectionType;
import app.nzyme.core.util.TimeRange;
import app.nzyme.plugin.Subsystem;
import com.google.common.collect.Maps;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

public class AssetManager {

    public enum OrderColumn {

        LAST_SEEN("last_seen"),
        MAC("mac");

        private final String columnName;

        OrderColumn(String columnName) {
            this.columnName = columnName;
        }

        public String getColumnName() {
            return columnName;
        }

    }

    private final NzymeNode nzyme;

    public AssetManager(NzymeNode nzyme) {
        this.nzyme = nzyme;
    }

    public long countAssets(TimeRange timeRange, UUID organizationId, UUID tenantId) {
        return nzyme.getDatabase().withHandle(handle ->
                handle.createQuery("SELECT COUNT(*) FROM assets WHERE organization_id = :organization_id " +
                                "AND tenant_id = :tenant_id AND last_seen >= :tr_from AND last_seen <= :tr_to")
                        .bind("organization_id", organizationId)
                        .bind("tenant_id", tenantId)
                        .bind("tr_from", timeRange.from())
                        .bind("tr_to", timeRange.to())
                        .mapTo(Long.class)
                        .one()
        );
    }

    public List<AssetEntry> findAllAssets(UUID organizationId,
                                          UUID tenantId,
                                          TimeRange timeRange,
                                          int limit,
                                          int offset,
                                          OrderColumn orderColumn,
                                          OrderDirection orderDirection) {
        return nzyme.getDatabase().withHandle(handle ->
                handle.createQuery("SELECT COUNT(*) FROM assets WHERE organization_id = :organization_id " +
                                "AND tenant_id = :tenant_id AND last_seen >= :tr_from AND last_seen <= :tr_to " +
                                "ORDER BY <order_column> <order_direction> " +
                                "LIMIT :limit OFFSET :offset")
                        .bind("organization_id", organizationId)
                        .bind("tenant_id", tenantId)
                        .bind("tr_from", timeRange.from())
                        .bind("tr_to", timeRange.to())
                        .bind("limit", limit)
                        .bind("offset", offset)
                        .define("order_column", orderColumn.getColumnName())
                        .define("order_direction", orderDirection)
                        .mapTo(AssetEntry.class)
                        .list()
        );
    }

    public Optional<AssetEntry> findAssetByMac(String mac, UUID organizationId, UUID tenantId) {
        return nzyme.getDatabase().withHandle(handle ->
                handle.createQuery("SELECT * FROM assets WHERE mac = :mac " +
                                "AND organization_id = :organization_id AND tenant_id = :tenant_id")
                        .bind("mac", mac)
                        .bind("organization_id", organizationId)
                        .bind("tenant_id", tenantId)
                        .mapTo(AssetEntry.class)
                        .findOne()
        );
    }

    public void onNewAsset(Subsystem subsystem,
                           UUID assetUuid,
                           String mac,
                           UUID organizationId,
                           UUID tenantId,
                           UUID tapUuid) {
        Map<String, String> attributes = Maps.newHashMap();
        attributes.put("asset_uuid", assetUuid.toString());
        attributes.put("mac", mac);

        // TODO only if alert enabled.
        nzyme.getDetectionAlertService().raiseAlert(
                organizationId,
                tenantId,
                null,
                tapUuid,
                DetectionType.ASSETS_NEW,
                subsystem,
                "New asset with MAC address \"" + mac + "\" detected.",
                attributes,
                new String[]{"asset_uuid"},
                null
        );
    }

}
