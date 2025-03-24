package app.nzyme.core.integrations.tenant.cot;

import app.nzyme.core.NzymeNode;
import app.nzyme.core.integrations.ScheduledIntegration;
import app.nzyme.core.integrations.ScheduledIntegrationConfiguration;

import java.util.concurrent.TimeUnit;

public class CotOutput implements ScheduledIntegration {

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
               // Load all configured outputs.

               // Send data to each output.

                    // Taps.

                    // UAVs.
            }
        };
    }

    @Override
    public ScheduledIntegrationConfiguration getConfiguration() {
        return CONFIGURATION;
    }

}
