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

package horse.wtf.nzyme.remote.forwarders;

import com.google.protobuf.ByteString;
import horse.wtf.nzyme.dot11.Dot11MetaInformation;
import horse.wtf.nzyme.dot11.frames.Dot11Frame;
import horse.wtf.nzyme.remote.protobuf.NzymeMessage;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.joda.time.DateTime;

import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetSocketAddress;
import java.net.SocketException;

public class UDPForwarder implements Forwarder {

    private static final Logger LOG = LogManager.getLogger(UDPForwarder.class);

    private final String nzymeId;
    private InetSocketAddress address;

    private DatagramSocket socket;

    public UDPForwarder(InetSocketAddress address, String nzymeId) {
        LOG.info("Initializing UDP forwarder to [{}]", address);
        this.address = address;
        this.nzymeId = nzymeId;

        try {
            socket = new DatagramSocket();
        } catch (SocketException e) {
            throw new RuntimeException("Could not create UDP socket.", e);
        }
    }

    @Override
    public void forward(Dot11Frame frame) {
        try {
            byte[] forwardedFrame= NzymeMessage.Message.newBuilder()
                    .setMessageType("frame")
                    .setFrame(NzymeMessage.Frame.newBuilder()
                            .setFrameType("802.11")
                            .setDot11Frame(
                                    NzymeMessage.Dot11Frame.newBuilder()
                                            .setRecordedAt(new DateTime().getMillis())
                                            .setSource(nzymeId)
                                            .setFrameType(frame.getClass().getCanonicalName())
                                            .setFrameHeader(ByteString.copyFrom(frame.header()))
                                            .setFramePayload(ByteString.copyFrom(frame.payload()))
                                            .setFrameMeta(buildMetaBuf(frame.meta()))
                                            .build()
                            ).build())
                    .build()
                    .toByteArray();

            socket.send(new DatagramPacket(forwardedFrame, forwardedFrame.length, address));
        } catch (Exception e) {
            LOG.error("Could not forward frame.", e);
        }
    }

    private NzymeMessage.FrameMeta buildMetaBuf(Dot11MetaInformation meta) {
        return NzymeMessage.FrameMeta.newBuilder()
                .setIsMalformed(meta.isMalformed())
                .setAntennaSignal(meta.getAntennaSignal())
                .setSignalQuality(meta.getSignalQuality())
                .setFrequency(meta.getFrequency())
                .setChannel(meta.getChannel())
                .setMacTimestamp(meta.getMacTimestamp())
                .setIsWEP(meta.isWep())
                .build();
    }

}
