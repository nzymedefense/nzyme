package app.nzyme.core.integrations.tenant.cot;

import app.nzyme.core.NzymeNode;
import app.nzyme.core.crypto.Crypto;
import app.nzyme.core.integrations.ScheduledIntegration;
import app.nzyme.core.integrations.ScheduledIntegrationConfiguration;
import app.nzyme.core.integrations.tenant.cot.db.CotOutputEntry;
import app.nzyme.core.integrations.tenant.cot.protocol.CotContact;
import app.nzyme.core.integrations.tenant.cot.protocol.CotEvent;
import app.nzyme.core.integrations.tenant.cot.protocol.CotEventDetail;
import app.nzyme.core.integrations.tenant.cot.protocol.CotPoint;
import app.nzyme.core.integrations.tenant.cot.transports.CotTransport;
import app.nzyme.core.integrations.tenant.cot.transports.CotTransportException;
import app.nzyme.core.integrations.tenant.cot.transports.CotTransportFactory;
import app.nzyme.core.integrations.tenant.cot.transports.CotTransportType;
import app.nzyme.core.taps.Tap;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;

import java.util.concurrent.TimeUnit;

public class CotOutput implements ScheduledIntegration {

    private static final Logger LOG = LogManager.getLogger(ScheduledIntegration.class);

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
                            // Decrypt certificate if there is one.
                            byte[] decryptedCertificate = null;
                            if (output.certificate() != null) {
                                decryptedCertificate = nzyme.getCrypto().decryptWithClusterKey(output.certificate());
                            }

                            // Taps.
                            for (Tap tap : nzyme.getTapManager()
                                    .findAllTapsOfTenant(output.organizationId(), output.tenantId())) {
                                CotTransport transport = CotTransportFactory.buildTransport(
                                        CotTransportType.valueOf(output.connectionType()),
                                        output.address(),
                                        output.port(),
                                        decryptedCertificate
                                );

                                if (tap.latitude() != null && tap.longitude() != null
                                        && tap.lastReport() != null
                                        && tap.lastReport().isAfter(DateTime.now().minusMinutes(2))) {

                                    DateTime now = DateTime.now(DateTimeZone.UTC);
                                    CotEvent event = CotEvent.create(
                                            "2.0",
                                            tap.uuid().toString(),
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
                                                    CotContact.create(tap.name())
                                            )
                                    );

                                    // Send to output.
                                    try {
                                        transport.sendEvent(event);
                                    } catch (CotTransportException e) {
                                        LOG.error("Could not send CoT message to CoT output [{}/{}]. Skipping.",
                                                output.uuid(), output.name(), e);
                                        continue;
                                    }

                                    // Count up stats if success.
                                }
                            }

                            // UAVs.

                        } catch (Crypto.CryptoOperationException | Exception e) {
                            LOG.error("Could not send message to CoT output [{}/{}]. Skipping.",
                                    output.uuid(), output.name());
                        }
                    }


                    // Send data to each output.

                    // Taps.

                    // UAVs.
                } catch(Exception e) {
                    LOG.error("Error in CotOutput.", e);
                }
            }
        };
    }

    @Override
    public ScheduledIntegrationConfiguration getConfiguration() {
        return CONFIGURATION;
    }

}
