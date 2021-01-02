/*
 * This file is part of nzyme.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the Server Side Public License, version 1,
 * as published by MongoDB, Inc.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * Server Side Public License for more details.
 *
 * You should have received a copy of the Server Side Public License
 * along with this program. If not, see
 * <http://www.mongodb.com/licensing/server-side-public-license>.
 */

package horse.wtf.nzyme.dot11;

import com.google.common.collect.ImmutableMap;
import org.pcap4j.util.ByteArrays;

import java.nio.ByteOrder;

public class Dot11LeavingReason {

    private static final short DEAUTH_CONTENT_LENGTH = 24;

    private static final ImmutableMap<Integer, String> REASONS = new ImmutableMap.Builder<Integer, String>()
            .put(0, "Reserved")
            .put(1, "Unspecified reason")
            .put(2, "Previous authentication no longer valid")
            .put(3, "Deauthenticated because sending STA is leaving (or has left) IBSS or ESS")
            .put(4, "Disassociated due to inactivity")
            .put(5, "Disassociated because AP is unable to handle all currently associated STAs")
            .put(6, "Class 2 frame received from nonauthenticated STA")
            .put(7, "Class 3 frame received from nonassociated STA")
            .put(8, "Disassociated because sending STA is leaving (or has left) BSS")
            .put(9, "STA requesting (re)association is not authenticated with responding STA")
            .put(10, "Disassociated because the information in the Power Capability element is unacceptable")
            .put(11, "Disassociated because the information in the Supported Channels element is unacceptable")
            .put(12, "Disassociated due to BSS Transition Management")
            .put(13, "Invalid element i.e. an element defined in this standard for which the content does not meet the specifications in Clause 8")
            .put(14, "Message integrity code (MIC) failure")
            .put(15, "4-Way Handshake timeout")
            .put(16, "Group Key Handshake timeout")
            .put(17, "Element in 4-Way Handshake different from (Re)Association Request/Probe Response/Beacon frame")
            .put(18, "Invalid group cypher")
            .put(19, "Invalid pairwise cypher")
            .put(20, "Invalid AKMP")
            .put(21, "Unsupported RSNE version")
            .put(22, "Invalid RSNE capabilities")
            .put(23, "IEEE 802.1X authentication failed")
            .put(24, "Cipher suite rejected because of the security policy")
            .put(25, "TDLS direct-link teardown due to TDLS peer STA unreachable via the TDLS direct link")
            .put(26, "TDLS direct-link teardown for unspecified reason")
            .put(27, "Disassociated because session terminated by SSP request")
            .put(28, "Disassociated because of lack of SSP roaming agreement")
            .put(29, "Requested service rejected because of SSP cipher suite or AKM requirement")
            .put(30, "Requested service not authorized in this location")
            .put(31, "TS deleted because QoS AP lacks sufficient bandwidth for this QoS STA due to change in BSS service characteristics or operational mode (e.g. an HT BSS change from 40 MHz channel to 20 MHz channel")
            .put(32, "Disassociated for unspecified, QoS-related reason")
            .put(33, "Disassociated because QoS AP lacks sufficient bandwidth for this QoS STA")
            .put(34, "Disassociated because excessive number of frames need to be acknowledged, but are not acknowledged due to AP transmissions and/or poor channel conditions")
            .put(35, "Disassociated because STA is transmitting outside the limits of TXOPs")
            .put(36, "Requested from peer STA as the STA is leaving the BSS (or resetting)")
            .put(37, "Requested from peer STA as the STA does not want to use the mechanism")
            .put(38, "Requested from peer STA as the STA received frames using the mechanism for which setup is required")
            .put(39, "Requested from peer STA due to timeout")
            .put(45, "Peer STA does not support the requested cipher suite")
            .put(46, "Disassociated because authorized access limit reached")
            .put(47, "Disassociated due to external service requirements")
            .put(48, "Invalid FT Action frame count")
            .put(49, "Invalid pairwise master key identified (PMKI)")
            .put(50, "Invalid MDE")
            .put(51, "Invalid FTE")
            .put(52, "SME cancels the mesh peering instance with the reason other than reaching the maximum number of peer mesh STAs")
            .put(53, "The mesh STA has reached the supported maximum number of peer STAs")
            .put(54, "The received information violates the Mesh Configuration policy configured in the mesh STA profile")
            .put(55, "The mesh STA has received a Mesh Peering Close message requesting to close the mesh peering")
            .put(56, "The mesh STA has resent dot11MeshMaxRetries Mesh Peering Open messages, without receiving a Mesh Peering Confirm message")
            .put(57, "The confirmTimer for the mesh peering instance times out")
            .put(58, "The mesh STA fails to unwrap the GTK or the values in the wrapped contents do not match")
            .put(59, "The mesh STA receives inconsistent information about the mesh parameters between Mesh Peering Management frames")
            .put(60, "The mesh STA fails the authenticated mesh peering exchange because due to failure in selecting either the pairwise ciphersuite or group ciphersuite")
            .put(61, "The mesh STA does not have proxy information for this external destination")
            .put(62, "The mesh STA does not have forwarding information for this destination")
            .put(63, "The mesh STA determines that the link to the next hop of an active path in its forwarding information is no longer usable")
            .put(64, "The Deauthentication frame was sent because the MAC address of the STA already exists in the mesh BSS.")
            .put(65, "The mesh STA performs channel switch to meet regulatory requirements")
            .put(66, "The mesh STA performs channel switch with unspecified reason")
            .build();

    public static String lookup(int reasonCode) {
        if (REASONS.containsKey(reasonCode)) {
            return REASONS.get(reasonCode);
        } else {
            return "Unknown reason (" + reasonCode + ")";
        }
    }

    public static short extract(byte[] payload, byte[] header) {
        if(payload.length < DEAUTH_CONTENT_LENGTH+1) {
            return -1;
        }

        byte[] reasonBytes = {
                payload[DEAUTH_CONTENT_LENGTH],
                payload[DEAUTH_CONTENT_LENGTH +1]
        };

        return ByteArrays.getShort(reasonBytes, 0, ByteOrder.LITTLE_ENDIAN);
    }

}
