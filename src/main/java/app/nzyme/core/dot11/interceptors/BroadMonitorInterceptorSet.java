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

package app.nzyme.core.dot11.interceptors;

import app.nzyme.core.NzymeNode;
import com.google.common.base.Strings;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Maps;
import app.nzyme.core.alerts.Alert;
import app.nzyme.core.dot11.Dot11FrameInterceptor;
import app.nzyme.core.dot11.Dot11FrameSubtype;
import app.nzyme.core.dot11.Dot11MetaInformation;
import app.nzyme.core.dot11.frames.*;
import app.nzyme.core.notifications.FieldNames;
import app.nzyme.core.notifications.Notification;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.Collections;
import java.util.List;
import java.util.Map;

public class BroadMonitorInterceptorSet {

    private static final Logger LOG = LogManager.getLogger(BroadMonitorInterceptorSet.class);

    private final NzymeNode nzyme;

    public BroadMonitorInterceptorSet(NzymeNode nzyme) {
        this.nzyme = nzyme;
    }

    public List<Dot11FrameInterceptor> getInterceptors() {
        ImmutableList.Builder<Dot11FrameInterceptor> interceptors = new ImmutableList.Builder<>();

        interceptors.add(new Dot11FrameInterceptor<Dot11DisassociationFrame>() {
            @Override
            public void intercept(Dot11DisassociationFrame frame) {
                String message = frame.transmitter() + " is disassociating from " + frame.destination() + " (" + frame.reasonString() + ")";
                nzyme.notifyUplinks(
                        new Notification(message, frame.meta().getChannel())
                                .addField(FieldNames.TRANSMITTER, frame.transmitter())
                                .addField(FieldNames.DESTINATION, frame.destination())
                                .addField(FieldNames.REASON_CODE, frame.reasonCode())
                                .addField(FieldNames.REASON_STRING, frame.reasonString())
                                .addField(FieldNames.SUBTYPE, "disassoc"),
                        frame.meta()
                );
                nzyme.forwardFrame(frame);
                LOG.debug(message);
            }
            @Override
            public byte forSubtype() {
                return Dot11FrameSubtype.DISASSOCIATION;
            }
            @Override
            public List<Class<? extends Alert>> raisesAlerts() {
                return Collections.emptyList();
            }
        });

        interceptors.add(new Dot11FrameInterceptor<Dot11AssociationRequestFrame>() {
            @Override
            public void intercept(Dot11AssociationRequestFrame frame) {
                nzyme.getClients().registerAssociationRequestFrame(frame);

                String message = frame.transmitter() + " is requesting to associate with "
                        + frame.ssid()
                        + " at " + frame.destination();
                nzyme.notifyUplinks(
                        new Notification(message, frame.meta().getChannel())
                                .addField(FieldNames.TRANSMITTER, frame.transmitter())
                                .addField(FieldNames.DESTINATION, frame.destination())
                                .addField(FieldNames.SSID, frame.ssid() == null ? "[no SSID]" : frame.ssid())
                                .addField(FieldNames.SUBTYPE, "assoc-req"),
                        frame.meta()
                );
                nzyme.forwardFrame(frame);
                LOG.debug(message);
            }
            @Override
            public byte forSubtype() {
                return Dot11FrameSubtype.ASSOCIATION_REQUEST;
            }
            @Override
            public List<Class<? extends Alert>> raisesAlerts() {
                return Collections.emptyList();
            }
        });

        interceptors.add(new Dot11FrameInterceptor<Dot11AssociationResponseFrame>() {
            @Override
            public void intercept(Dot11AssociationResponseFrame frame) {
                String message = frame.transmitter() + " answered association request from " + frame.destination()
                        + ". Response: " + frame.response().toUpperCase() + " (" + frame.responseCode() + ")";
                nzyme.notifyUplinks(
                        new Notification(message, frame.meta().getChannel())
                                .addField(FieldNames.TRANSMITTER, frame.transmitter())
                                .addField(FieldNames.DESTINATION, frame.destination())
                                .addField(FieldNames.RESPONSE_CODE, frame.responseCode())
                                .addField(FieldNames.RESPONSE_STRING, frame.response())
                                .addField(FieldNames.SUBTYPE, "assoc-resp"),
                        frame.meta()
                );
                nzyme.forwardFrame(frame);
                LOG.debug(message);
            }
            @Override
            public byte forSubtype() {
                return Dot11FrameSubtype.ASSOCIATION_RESPONSE;
            }
            @Override
            public List<Class<? extends Alert>> raisesAlerts() {
                return Collections.emptyList();
            }
        });

        interceptors.add(new Dot11FrameInterceptor<Dot11AuthenticationFrame>() {
            @Override
            public void intercept(Dot11AuthenticationFrame frame) {
                String message = "";
                Map<String, Object> additionalFields = Maps.newHashMap();
                switch(frame.algorithm()) {
                    case OPEN_SYSTEM:
                        switch(frame.transactionSequence()) {
                            case 1:
                                message = frame.transmitter() + " is requesting to authenticate with Open System (Open, WPA, WPA2, ...) " +
                                        "at " + frame.destination();
                                break;
                            case 2:
                                message = frame.transmitter() + " is responding to Open System (Open, WPA, WPA2, ...) authentication " +
                                        "request from " + frame.destination() + ". (" + frame.statusString() + ")";
                                additionalFields.put(FieldNames.RESPONSE_CODE, frame.statusCode());
                                additionalFields.put(FieldNames.RESPONSE_STRING, frame.statusString());
                                break;
                            default:
                                LOG.trace("Invalid Open System authentication transaction sequence number [{}]. " +
                                        "Skipping.", frame.transactionSequence());
                                return;
                        }
                        break;
                    case SAE:
                        switch(frame.transactionSequence()) {
                            case 1:
                                message = frame.transmitter() + " is requesting to authenticate using SAE (WPA3) at " + frame.destination();
                                break;
                            case 2:
                                message = frame.transmitter() + " is responding to SAE (WPA3) authentication " +
                                        "request from " + frame.destination() + ". (" + frame.statusString() + ")";
                                additionalFields.put(FieldNames.RESPONSE_CODE, frame.statusCode());
                                additionalFields.put(FieldNames.RESPONSE_STRING, frame.statusString());
                                break;
                            default:
                                LOG.trace("Invalid SAE authentication transaction sequence number [{}]. " +
                                        "Skipping.", frame.transactionSequence());
                                return;
                        }
                        break;
                    case SHARED_KEY:
                        switch (frame.transactionSequence()) {
                            case 1:
                                message = frame.transmitter() + " is requesting to authenticate using WEP at " + frame.destination();
                                break;
                            case 2:
                                message = frame.transmitter() + " is responding to WEP authentication request at " +
                                        frame.destination() + " with clear text challenge.";
                                break;
                            case 4:
                                message = frame.transmitter() + " is responding to WEP authentication request from " +
                                        frame.destination() + ". (" + frame.statusString() + ")";
                                additionalFields.put(FieldNames.RESPONSE_CODE, frame.statusCode());
                                additionalFields.put(FieldNames.RESPONSE_STRING, frame.statusString());
                                break;
                            default:
                                LOG.trace("Invalid WEP authentication transaction sequence number [{}]. " +
                                        "Skipping.", frame.transactionSequence());
                                return;
                        }
                        break;
                }
                nzyme.notifyUplinks(new Notification(message, frame.meta().getChannel())
                        .addField(FieldNames.TRANSMITTER, frame.transmitter())
                        .addField(FieldNames.DESTINATION, frame.destination())
                        .addField(FieldNames.AUTH_ALGORITHM, frame.algorithm().toString().toLowerCase())
                        .addField(FieldNames.TRANSACTION_SEQUENCE_NUMBER, frame.transactionSequence())
                        .addField(FieldNames.SUBTYPE, "auth")
                        .addFields(additionalFields), frame.meta());
                nzyme.forwardFrame(frame);
                LOG.debug(message);
            }
            @Override
            public byte forSubtype() {
                return Dot11FrameSubtype.AUTHENTICATION;
            }
            @Override
            public List<Class<? extends Alert>> raisesAlerts() {
                return Collections.emptyList();
            }
        });

        interceptors.add(new Dot11FrameInterceptor<Dot11BeaconFrame>() {
            @Override
            public void intercept(Dot11BeaconFrame frame) {
                nzyme.getNetworks().registerBeaconFrame(frame);

                String message;
                if (!Strings.isNullOrEmpty(frame.ssid())) {
                    message = "Received beacon from " + frame.transmitter() + " for SSID " + frame.ssid();
                } else {
                    // Broadcast beacon.
                    message = "Received broadcast beacon from " + frame.transmitter();
                }
                Dot11MetaInformation meta = frame.meta();
                nzyme.notifyUplinks(
                        new Notification(message, frame.meta().getChannel())
                                .addField(FieldNames.TRANSMITTER, frame.transmitter())
                                .addField(FieldNames.TRANSMITTER_FINGERPRINT, frame.transmitterFingerprint())
                                .addField(FieldNames.SSID, Strings.isNullOrEmpty(frame.ssid()) ? "[no SSID]" : frame.ssid())
                                .addField(FieldNames.SECURITY_FULL, frame.taggedParameters().getFullSecurityString())
                                .addField(FieldNames.IS_WPA1, frame.taggedParameters().isWPA1())
                                .addField(FieldNames.IS_WPA2, frame.taggedParameters().isWPA2())
                                .addField(FieldNames.IS_WPA3, frame.taggedParameters().isWPA3())
                                .addField(FieldNames.IS_WPS, frame.taggedParameters().isWPS())
                                .addField(FieldNames.SUBTYPE, "beacon"),
                        meta
                );
                nzyme.forwardFrame(frame);
                LOG.debug(message);
            }
            @Override
            public byte forSubtype() {
                return Dot11FrameSubtype.BEACON;
            }
            @Override
            public List<Class<? extends Alert>> raisesAlerts() {
                return Collections.emptyList();
            }
        });

        interceptors.add(new Dot11FrameInterceptor<Dot11DeauthenticationFrame>() {
            @Override
            public void intercept(Dot11DeauthenticationFrame frame) {
                String message = "Deauth: Transmitter " + frame.transmitter() + " is deauthenticating " + frame.destination()
                        + " from BSSID " + frame.bssid() + " (" + frame.reasonString() + ")";
                nzyme.notifyUplinks(
                        new Notification(message, frame.meta().getChannel())
                                .addField(FieldNames.TRANSMITTER, frame.transmitter())
                                .addField(FieldNames.DESTINATION, frame.destination())
                                .addField(FieldNames.BSSID, frame.bssid())
                                .addField(FieldNames.REASON_CODE, frame.reasonCode())
                                .addField(FieldNames.REASON_STRING, frame.reasonString())
                                .addField(FieldNames.SUBTYPE, "deauth"),
                        frame.meta()
                );
                nzyme.forwardFrame(frame);
                LOG.debug(message);
            }
            @Override
            public byte forSubtype() {
                return Dot11FrameSubtype.DEAUTHENTICATION;
            }
            @Override
            public List<Class<? extends Alert>> raisesAlerts() {
                return Collections.emptyList();
            }
        });

        interceptors.add(new Dot11FrameInterceptor<Dot11ProbeRequestFrame>() {
            @Override
            public void intercept(Dot11ProbeRequestFrame frame) {
                nzyme.getClients().registerProbeRequestFrame(frame);

                String message;
                if(!frame.isBroadcastProbe()) {
                    message = "Probe request: " + frame.requester() + " is looking for " + frame.ssid();
                } else {
                    message = "Probe request: " + frame.requester() + " is looking for any network. (null probe request)";
                }
                nzyme.notifyUplinks(
                        new Notification(message, frame.meta().getChannel())
                                .addField(FieldNames.SSID, frame.ssid() == null ? "[no SSID]" : frame.ssid())
                                .addField(FieldNames.TRANSMITTER, frame.requester())
                                .addField(FieldNames.SUBTYPE, "probe-req"),
                        frame.meta()
                );
                nzyme.forwardFrame(frame);
                LOG.debug(message);
            }
            @Override
            public byte forSubtype() {
                return Dot11FrameSubtype.PROBE_REQUEST;
            }
            @Override
            public List<Class<? extends Alert>> raisesAlerts() {
                return Collections.emptyList();
            }
        });

        interceptors.add(new Dot11FrameInterceptor<Dot11ProbeResponseFrame>() {
            @Override
            public void intercept(Dot11ProbeResponseFrame frame) {
                nzyme.getNetworks().registerProbeResponseFrame(frame);

                String message;
                if (frame.ssid() == null) {
                    message = frame.transmitter() + " responded to broadcast probe request from " + frame.destination();
                } else {
                    message = frame.transmitter() + " responded to probe request from " + frame.destination() + " for " + frame.ssid();
                }
                nzyme.notifyUplinks(
                        new Notification(message, frame.meta().getChannel())
                                .addField(FieldNames.DESTINATION, frame.destination())
                                .addField(FieldNames.TRANSMITTER, frame.transmitter())
                                .addField(FieldNames.SSID, frame.ssid() == null ? "[no SSID]" : frame.ssid())
                                .addField(FieldNames.SECURITY_FULL, frame.taggedParameters().getFullSecurityString())
                                .addField(FieldNames.IS_WPA1, frame.taggedParameters().isWPA1())
                                .addField(FieldNames.IS_WPA2, frame.taggedParameters().isWPA2())
                                .addField(FieldNames.IS_WPA3, frame.taggedParameters().isWPA3())
                                .addField(FieldNames.IS_WPS, frame.taggedParameters().isWPS())
                                .addField(FieldNames.SUBTYPE, "probe-resp"),
                        frame.meta()
                );
                nzyme.forwardFrame(frame);
                LOG.debug(message);
            }
            @Override
            public byte forSubtype() {
                return Dot11FrameSubtype.PROBE_RESPONSE;
            }
            @Override
            public List<Class<? extends Alert>> raisesAlerts() {
                return Collections.emptyList();
            }
        });

        return interceptors.build();
    }

}
