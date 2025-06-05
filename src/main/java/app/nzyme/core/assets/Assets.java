package app.nzyme.core.assets;

import app.nzyme.core.NzymeNode;
import app.nzyme.core.assets.db.AssetEntry;

import java.util.Optional;
import java.util.UUID;

public class Assets {

    private final NzymeNode nzyme;

    public Assets(NzymeNode nzyme) {
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

}
