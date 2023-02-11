package app.nzyme.core.monitoring.health.indicators;

import app.nzyme.core.crypto.Crypto;
import app.nzyme.core.monitoring.health.Indicator;
import app.nzyme.core.monitoring.health.db.IndicatorStatus;

public class CryptoSyncIndicator extends Indicator {

    private final Crypto crypto;

    public CryptoSyncIndicator(Crypto crypto) {
        this.crypto = crypto;
    }

    @Override
    protected IndicatorStatus doRun() {
        return crypto.allPGPKeysEqualAcrossCluster()
                ? IndicatorStatus.green(this) : IndicatorStatus.red(this);
    }

    @Override
    public String getId() {
        return "crypto_sync";
    }

    @Override
    public String getName() {
        return "Crypto Sync";
    }

}
