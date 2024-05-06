package app.nzyme.core.ethernet.tcp;

public enum TcpSessionState {

    SYNSENT,
    SYNRECEIVED,
    ESTABLISHED,
    FINWAIT1,
    FINWAIT2,
    CLOSEDFIN,
    CLOSEDRST,
    CLOSEDTIMEOUT,
    REFUSED

}
