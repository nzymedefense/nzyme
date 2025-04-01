package app.nzyme.core.integrations.tenant.cot.transports;

public class CotTransportException extends Throwable {

    public CotTransportException(String message) {
        super(message);
    }

    public CotTransportException(String message, Throwable cause) {
        super(message, cause);
    }

}
