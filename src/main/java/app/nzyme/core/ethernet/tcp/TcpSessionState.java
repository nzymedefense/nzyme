package app.nzyme.core.ethernet.tcp;

public enum TcpSessionState {

    /*
     * Other places to consider when making any changes here:
     *
     * * EthernetConnectionCleaner
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
    REFUSED

}
