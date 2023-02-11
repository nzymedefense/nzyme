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

package app.nzyme.core.remote.inputs;

import app.nzyme.core.NzymeNode;
import com.codahale.metrics.Meter;
import com.codahale.metrics.Timer;
import com.google.protobuf.InvalidProtocolBufferException;
import app.nzyme.core.dot11.MalformedFrameException;
import app.nzyme.core.dot11.frames.Dot11FrameFactory;
import app.nzyme.core.processing.FrameProcessor;
import app.nzyme.core.remote.protobuf.NzymeMessage;
import app.nzyme.core.util.MetricNames;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.pcap4j.packet.IllegalRawDataException;

import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetSocketAddress;
import java.net.SocketException;
import java.util.Arrays;
import java.util.concurrent.atomic.AtomicBoolean;

public class RemoteFrameInput {

    private static final Logger LOG = LogManager.getLogger(RemoteFrameInput.class);

    private final FrameProcessor processor;
    private final Dot11FrameFactory frameFactory;

    private final InetSocketAddress address;

    // Metrics
    private final Meter remoteFramesReceived;
    private final Timer remoteFrameTimer;

    private final AtomicBoolean inLoop = new AtomicBoolean(false);

    private DatagramSocket socket;

    public RemoteFrameInput(NzymeNode nzyme, InetSocketAddress address) {
        this.address = address;
        this.processor = nzyme.getFrameProcessor();
        this.frameFactory = new Dot11FrameFactory(nzyme.getMetrics(), nzyme.getAnonymizer());

        this.remoteFramesReceived = nzyme.getMetrics().meter(MetricNames.REMOTE_FRAMES_RECEIVED);
        this.remoteFrameTimer = nzyme.getMetrics().timer(MetricNames.REMOTE_FRAMES_TIMING);
    }

    private void initialize() throws SocketException {
        if (this.socket != null && this.socket.isBound()) {
            this.socket.close();
        }

        this.socket = new DatagramSocket(address);
    }

    public Runnable run() {
        return () -> {
            LOG.info("Starting remote frame input at [{}].", address);

            while (true) {
                try {
                    if(!inLoop.get()) {
                        initialize();
                    }
                } catch (Exception e) {
                    inLoop.set(false);

                    LOG.error("Could not initialize remote input at [{}]. Retrying soon.", address, e);
                    // Try again with delay.
                    try {
                        Thread.sleep(2500);
                    } catch (InterruptedException ex) { /* noop */ }

                    continue;
                }

                // We are in the loop and active if we reach here.
                inLoop.set(true);

                try {
                    byte[] buffer = new byte[4096];
                    DatagramPacket packet = new DatagramPacket(buffer, buffer.length);
                    socket.receive(packet);

                    Timer.Context time = this.remoteFrameTimer.time();
                    byte[] payload = Arrays.copyOfRange(packet.getData(), 0, packet.getLength());

                    NzymeMessage.Message message = NzymeMessage.Message.parseFrom(payload);

                    if (!message.getMessageType().equals("frame")) {
                        LOG.trace("Ignoring frame type [{}].", message.getMessageType());
                        continue;
                    }

                    if (!message.getFrame().getFrameType().equals("802.11")) {
                        LOG.trace("Ignoring frame type [{}].", message.getFrame().getFrameType());
                        continue;
                    }

                    this.remoteFramesReceived.mark();

                    NzymeMessage.Dot11Frame frame = message.getFrame().getDot11Frame();
                    processor.processDot11Frame(frameFactory.fromRemote(frame));
                    time.stop();
                } catch (MalformedFrameException | IllegalRawDataException | InvalidProtocolBufferException e) {
                    LOG.warn("Invalid content of received remote frame. Skipping but not resetting connection.", e);
                    continue;
                } catch (Exception e) {
                    LOG.warn("Error receiving remote frame. Skipping.", e);
                    inLoop.set(false);
                    continue;
                }
            }
        };
    }

}
