package app.nzyme.core.dot11.monitoring;

import app.nzyme.core.NzymeNode;
import app.nzyme.core.detection.alerts.DetectionType;
import app.nzyme.core.detection.alerts.Subsystem;
import app.nzyme.core.dot11.db.BSSIDWithTap;
import app.nzyme.core.taps.Tap;
import com.google.common.collect.Maps;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.Map;
import java.util.Optional;

public class Dot11BanditDetector {

    private static final Logger LOG = LogManager.getLogger(Dot11BanditDetector.class);

    private final NzymeNode nzyme;

    public Dot11BanditDetector(NzymeNode nzyme) {
        this.nzyme = nzyme;
    }

    public void run() {
        for (Dot11BanditDescription bandit : Dot11Bandits.BUILT_IN) {
            for (BSSIDWithTap match : nzyme.getDot11().findAllBSSIDSOfAllTenantsWithFingerprint(1, bandit.fingerprint())) {
                LOG.debug("Detected bandit [{}] at [{}].", bandit, match);

                Optional<Tap> tap = nzyme.getTapManager().findTap(match.tapUUID());

                if (tap.isEmpty()) {
                    LOG.error("Detected bandit [{}] but could not find associated tap [{}]. Skipping.",
                            bandit.name(), match.tapUUID());
                    continue;
                }

                Map<String, String> attributes = Maps.newHashMap();
                attributes.put("fingerprint", bandit.fingerprint());
                attributes.put("bssid", match.bssid());
                attributes.put("tap_uuid", match.tapUUID().toString());
                attributes.put("bandit_name", bandit.name());
                attributes.put("bandit_description", bandit.description());

                nzyme.getDetectionAlertService().raiseAlert(
                        tap.get().organizationId(),
                        tap.get().tenantId(),
                        null,
                        tap.get().uuid(),
                        DetectionType.DOT11_BANDIT_CONTACT,
                        Subsystem.DOT11,
                        attributes,
                        new String[]{"bssid", "fingerprint", "tap_uuid"}
                );
            }
        }
    }

}
