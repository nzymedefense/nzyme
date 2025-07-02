package app.nzyme.core.ethernet.l4.udp;

public enum UdpConversationState {

    /*
     * Other places to consider when making any changes here:
     *
     * * EthernetConnectionCleaner
     */

    ACTIVE,
    CLOSED,
    CLOSEDNODE

}
