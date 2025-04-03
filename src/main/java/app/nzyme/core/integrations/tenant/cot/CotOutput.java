package app.nzyme.core.integrations.tenant.cot;

import app.nzyme.core.NzymeNode;
import app.nzyme.core.crypto.Crypto;
import app.nzyme.core.integrations.ScheduledIntegration;
import app.nzyme.core.integrations.ScheduledIntegrationConfiguration;
import app.nzyme.core.integrations.tenant.cot.db.CotOutputEntry;
import app.nzyme.core.integrations.tenant.cot.protocol.*;
import app.nzyme.core.integrations.tenant.cot.transports.*;
import app.nzyme.core.shared.Classification;
import app.nzyme.core.taps.Tap;
import app.nzyme.core.uav.db.UavEntry;
import app.nzyme.core.uav.util.RemoteIdAccuracies;
import app.nzyme.core.util.TimeRangeFactory;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;

import java.util.concurrent.TimeUnit;

public class CotOutput implements ScheduledIntegration {

    private static final Logger LOG = LogManager.getLogger(CotOutput.class);

    private static final double NO_ACCURACY = 9999999.0;

    private static final ScheduledIntegrationConfiguration CONFIGURATION = ScheduledIntegrationConfiguration.create(
            10, 10, TimeUnit.SECONDS
    );

    private final NzymeNode nzyme;

    public CotOutput(NzymeNode nzyme) {
        this.nzyme = nzyme;
    }

    public void initialize() {
        // NOOP
    }

    public Runnable execute() {
        return new Runnable() {
            @Override
            public void run() {
                try {
                    // Load all configured outputs.
                    for (CotOutputEntry output : nzyme.getCotService().findAllOutputsOfAllTenants()) {
                        try {
                            if (!output.status().equals("RUNNING")) {
                                continue;
                            }

                            // Decrypt certificate if there is one.
                            byte[] decryptedCertificate = null;
                            if (output.certificate() != null) {
                                decryptedCertificate = nzyme.getCrypto().decryptWithClusterKey(output.certificate());
                            }
                            String decryptedCertificatePassphrase = null;
                            if (output.certificatePassphrase() != null) {
                                decryptedCertificatePassphrase = new String(
                                        nzyme.getCrypto().decryptWithClusterKey(output.certificatePassphrase())
                                );
                            }

                            CotTransport transport = CotTransportFactory.buildTransport(
                                    CotTransportType.valueOf(output.connectionType()),
                                    output.address(),
                                    output.port(),
                                    decryptedCertificate,
                                    decryptedCertificatePassphrase
                            );

                            // Taps.
                            for (Tap tap : nzyme.getTapManager()
                                    .findAllTapsOfTenant(output.organizationId(), output.tenantId())) {
                                if (tap.latitude() != null && tap.longitude() != null
                                        && tap.lastReport() != null
                                        && tap.lastReport().isAfter(DateTime.now().minusMinutes(2))) {

                                    DateTime now = DateTime.now(DateTimeZone.UTC);
                                    CotEvent event = CotEvent.create(
                                            "2.0",
                                            tap.uuid().toString(),
                                            "m-g",
                                            output.leafTypeTap(),
                                            now.toString(),
                                            now.toString(),
                                            now.plusMinutes(2).toString(),
                                            CotPoint.create(
                                                    tap.latitude(),
                                                    tap.longitude(),
                                                    0.0,
                                                    NO_ACCURACY,
                                                    NO_ACCURACY
                                            ),
                                            CotEventDetail.create(
                                                    "Nzyme Tap \"" + tap.name() + "\"",
                                                    null,
                                                    null,
                                                    CotContact.create(tap.name()),
                                                    null
                                            )
                                    );

                                    // Send to output.
                                    CotProcessingResult result;
                                    try {
                                        result = transport.sendEvent(event);
                                    } catch (CotTransportException e) {
                                        LOG.error("Could not send CoT message to CoT output [{}/{}]. Skipping.",
                                                output.uuid(), output.name(), e);
                                        continue;
                                    }

                                    // Count up stats if success.
                                    nzyme.getCotService().incrementOutputStats(
                                            output.id(), result.bytesSent(), result.messagesSent()
                                    );
                                }
                            }

                            // UAVs.
                            for (UavEntry uav : nzyme.getUav().findAllUavsOfTenant(
                                    TimeRangeFactory.oneMinute(), output.organizationId(), output.tenantId())) {
                                if (uav.latitude() == null || uav.longitude() == null) {
                                    continue;
                                }

                                DateTime now = DateTime.now(DateTimeZone.UTC);

                                CotRelation relation = null;
                                if (uav.operatorLatitude() != null && uav.operatorLongitude() != null) {
                                    relation = CotRelation.create("controlled-by", "UAV-OP-" + uav.identifier());
                                }

                                // Horizontal and vertical accuracy.
                                double horizontalAccuracy = 0.0;
                                double verticalAccuracy = 0.0;
                                if ((uav.detectionSource().equals("RemoteIdWiFi")
                                        || uav.detectionSource().equals("RemoteIdBluetooth"))
                                        && uav.accuracyHorizontal() != null
                                        && uav.accuracyVertical() != null) {
                                    horizontalAccuracy = RemoteIdAccuracies
                                            .horizontalAccuracyToCursorOnTargetMeters(uav.accuracyHorizontal());
                                    verticalAccuracy = RemoteIdAccuracies
                                            .verticalAccuracyToCursorOnTargetMeters(uav.accuracyVertical());
                                }

                                // Find an altitude if we have it.
                                String heightType = "none";
                                Double height = 0.0;
                                if (uav.height() != null && uav.heightType() != null) {
                                    if (uav.heightType().equals("AboveGround")) {
                                        heightType = "Above Ground Level";
                                        height = uav.height();
                                    }
                                } else if (uav.altitudePressure() != null) {
                                    heightType = "Pressure";
                                    height = uav.altitudePressure();
                                } else if (uav.altitudeGeodetic() != null) {
                                    heightType = "Geodetic";
                                    height = uav.altitudeGeodetic();
                                }

                                CotEvent event = CotEvent.create(
                                        "2.0",
                                        "UAV-" + uav.identifier(),
                                        "m-s",
                                        buildUavLeafType(uav),
                                        now.toString(),
                                        now.toString(),
                                        now.plusMinutes(2).toString(),
                                        CotPoint.create(
                                                uav.latitude(),
                                                uav.longitude(),
                                                height,
                                                horizontalAccuracy,
                                                verticalAccuracy
                                        ),
                                        CotEventDetail.create(
                                                "UAV \"" + uav.designation() + "\". Detected by Nzyme. " +
                                                        "Altitude type: " + heightType + ", Operational status: " +
                                                        uav.operationalStatus(),
                                                CotHeight.create(height),
                                                CotTrack.create(uav.groundTrack(), uav.speed()),
                                                CotContact.create("(UAV) " + uav.designation()),
                                                relation
                                        )
                                );

                                // Send to output.
                                CotProcessingResult result;
                                try {
                                    result = transport.sendEvent(event);
                                } catch (CotTransportException e) {
                                    LOG.error("Could not send CoT message to CoT output [{}/{}]. Skipping.",
                                            output.uuid(), output.name(), e);
                                    continue;
                                }

                                // Count up stats if success.
                                nzyme.getCotService().incrementOutputStats(
                                        output.id(), result.bytesSent(), result.messagesSent()
                                );

                                // UAV operator.
                                if (uav.operatorLongitude() != null && uav.operatorLatitude() != null) {
                                    CotEvent operatorEvent = CotEvent.create(
                                            "2.0",
                                            "UAV-OP-" + uav.identifier(),
                                            "m-s",
                                            buildOperatorLeafType(uav),
                                            now.toString(),
                                            now.toString(),
                                            now.plusMinutes(2).toString(),
                                            CotPoint.create(
                                                    uav.operatorLatitude(),
                                                    uav.operatorLongitude(),
                                                    uav.operatorAltitude() == null ? NO_ACCURACY : uav.operatorAltitude(),
                                                    NO_ACCURACY,
                                                    NO_ACCURACY
                                            ),
                                            CotEventDetail.create(
                                                    "Operator of UAV \"" + uav.designation() + "\". Detected " +
                                                            "by Nzyme. Height <" + uav.operatorAltitude() + "m> " +
                                                            "(Geodetic), Location type: " + uav.operatorLocationType(),
                                                    uav.operatorAltitude() == null
                                                            ? null : CotHeight.create(uav.operatorAltitude()),
                                                    null,
                                                    CotContact.create("(UAV-OP) " + uav.designation()),
                                                    null
                                            )
                                    );

                                    // Send to output.
                                    CotProcessingResult operatorResult;
                                    try {
                                        operatorResult = transport.sendEvent(operatorEvent);
                                    } catch (CotTransportException e) {
                                        LOG.error("Could not send CoT message to CoT output [{}/{}]. Skipping.",
                                                output.uuid(), output.name(), e);
                                        continue;
                                    }

                                    // Count up stats if success.
                                    nzyme.getCotService().incrementOutputStats(
                                            output.id(), operatorResult.bytesSent(), operatorResult.messagesSent()
                                    );
                                }
                            }


                        } catch (Crypto.CryptoOperationException | Exception e) {
                            LOG.error("Could not send message to CoT output [{}/{}]. Skipping.",
                                    output.uuid(), output.name(), e);
                        }
                    }
                } catch(Exception e) {
                    LOG.error("Error in CotOutput.", e);
                }
            }
        };
    }

    private String buildUavLeafType(UavEntry uav) {
        Classification classification = Classification.valueOf(uav.classification());

        String leaf = "a-";

        switch (classification) {
            case UNKNOWN -> leaf += "u";
            case FRIENDLY -> leaf += "f";
            case HOSTILE -> leaf += "h";
            case NEUTRAL -> leaf += "n";
        }

        leaf += "-A-M-";

        if (uav.uavType().equals("Aeroplane")) {
            leaf += "F"; // Fixed wing.
        } else {
            leaf += "H"; // Rotary.
        }

        leaf += "-Q"; // UAV.

        return leaf;
    }

    private String buildOperatorLeafType(UavEntry uav) {
        Classification classification = Classification.valueOf(uav.classification());
        String leaf = "a-";

        switch (classification) {
            case UNKNOWN -> leaf += "u";
            case FRIENDLY -> leaf += "f";
            case HOSTILE -> leaf += "h";
            case NEUTRAL -> leaf += "n";
        }

        leaf += "-G-E"; // UAV.

        return leaf;
    }

    @Override
    public ScheduledIntegrationConfiguration getConfiguration() {
        return CONFIGURATION;
    }

}
