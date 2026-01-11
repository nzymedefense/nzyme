package app.nzyme.core.ethernet.l4.tcp;

public enum TcpSessionState {

    /*
     * Other places to consider when making any changes here:
     *
     * * L4ConnectionCleaner
     */

    SYNSENT,
    SYNRECEIVED,
    ESTABLISHED,
    FINWAIT1,
    FINWAIT2,
    CLOSEDFIN,
    CLOSEDRST,
    CLOSEDTIMEOUT,
    CLOSEDTIMEOUTNODE,
    CLOSEDNODE,
    REFUSED,
    ACTIVE,
    CLOSED

}
