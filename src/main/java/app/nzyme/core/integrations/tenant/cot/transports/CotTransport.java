package app.nzyme.core.integrations.tenant.cot.transports;

import app.nzyme.core.integrations.tenant.cot.protocol.CotEvent;

public interface CotTransport {

    CotProcessingResult sendEvent(CotEvent event) throws CotTransportException;

}
