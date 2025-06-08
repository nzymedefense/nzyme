package app.nzyme.core.assets;

import app.nzyme.core.NzymeNode;
import app.nzyme.core.assets.db.AssetEntry;
import app.nzyme.core.detection.alerts.DetectionType;
import app.nzyme.plugin.Subsystem;
import com.google.common.collect.Maps;

import java.util.Map;
import java.util.Optional;
import java.util.UUID;

public class AssetManager {

    private final NzymeNode nzyme;

    public AssetManager(NzymeNode nzyme) {
        this.nzyme = nzyme;
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
