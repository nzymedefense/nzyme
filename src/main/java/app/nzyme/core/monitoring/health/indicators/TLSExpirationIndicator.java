package app.nzyme.core.monitoring.health.indicators;

import app.nzyme.core.NzymeNode;
import app.nzyme.core.crypto.Crypto;
import app.nzyme.core.crypto.tls.TLSKeyAndCertificate;
import app.nzyme.core.crypto.tls.TLSWildcardKeyAndCertificate;
import app.nzyme.core.distributed.Node;
import app.nzyme.core.distributed.NodeManager;
import app.nzyme.core.monitoring.health.Indicator;
import app.nzyme.core.monitoring.health.db.IndicatorStatus;
import org.joda.time.DateTime;

import java.util.Optional;

public class TLSExpirationIndicator extends Indicator  {

    private final Crypto crypto;
    private final NodeManager nodes;

    public TLSExpirationIndicator(Crypto crypto, NodeManager nodes) {
        this.crypto = crypto;
        this.nodes = nodes;
    }

    @Override
    protected IndicatorStatus doRun() {
        // Individual certificates.
        for (Node node : nodes.getNodes()) {
            Optional<TLSKeyAndCertificate> tls = crypto.getTLSCertificateOfNode(node.uuid());
            if (tls.isPresent()) {
                if (tls.get().expiresAt().isBefore(DateTime.now().plusDays(7))) {
                    return IndicatorStatus.red(this);
                }
            }
        }

        // Wildcard certificates.
        for (TLSWildcardKeyAndCertificate tls : crypto.getTLSWildcardCertificates()) {
            if (tls.expiresAt().isBefore(DateTime.now().plusDays(7))) {
                return IndicatorStatus.red(this);
            }
        }

        return IndicatorStatus.green(this);
    }

    @Override
    public String getId() {
        return "tls_exp";
    }

    @Override
    public String getName() {
        return "TLS Expiration";
    }

}
