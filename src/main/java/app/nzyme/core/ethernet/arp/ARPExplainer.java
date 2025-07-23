package app.nzyme.core.ethernet.arp;

public class ARPExplainer {

    public static String explain(String ethernetDestinationMac,
                                 String operation,
                                 String senderMac,
                                 String senderAddress,
                                 String targetMac,
                                 String targetAddress) {

        final boolean dstIsBroadcast = isBroadcast(ethernetDestinationMac);
        final boolean isGratuitous = senderAddress.equals(targetAddress);

        StringBuilder sb = new StringBuilder();

        switch (operation.trim().toLowerCase()) {
            case "request": {
                if (isGratuitous) {
                    // Gratuitous request.
                    sb.append("Gratuitous ARP Request: **")
                            .append(senderMac).append("** (**").append(senderAddress).append("**) ")
                            .append("announces its own IP address to the network");
                    if (!dstIsBroadcast) {
                        sb.append(" (unexpectedly sent unicast to **").append(ethernetDestinationMac).append("**)");
                    }
                } else {
                    // Ordinary request.
                    sb.append("ARP Request: **")
                            .append(senderMac).append("** (**").append(senderAddress).append("**) ")
                            .append("asks for the MAC address of **").append(targetAddress).append("**");
                }
                break;
            }
            case "reply": {
                if (isGratuitous) {
                    // Gratuitous reply.
                    sb.append("Gratuitous ARP Reply: **")
                            .append(senderMac).append("** (**").append(senderAddress).append("**) ")
                            .append("asserts ownership of **").append(senderAddress)
                            .append("** so all hosts update their ARP caches");
                    if (!dstIsBroadcast && !isBroadcast(targetMac)) {
                        sb.append(" (delivered unicast to **").append(targetMac).append("**)");
                    }
                } else {
                    // Ordinary reply.
                    sb.append("ARP Reply: **")
                            .append(senderMac).append("** (**").append(senderAddress).append("**) ")
                            .append("informs **").append(targetMac).append("** (**").append(targetAddress).append("**) ")
                            .append("that its MAC address is **").append(senderMac).append("**");
                    if (dstIsBroadcast) {
                        sb.append(" – note: reply sent as broadcast (unusual but legal)");
                    }
                }
                break;
            }

            default:
                sb.append("Unknown ARP operation \"").append(operation).append('"');
        }

        return sb.toString();
    }

    private static boolean isBroadcast(String mac) {
        return mac != null && mac.equalsIgnoreCase("ff:ff:ff:ff:ff:ff");
    }
}
