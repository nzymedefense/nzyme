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
import com.codahale.metrics.MetricRegistry;
import app.nzyme.core.MockNzyme;
import app.nzyme.core.dot11.MalformedFrameException;
import app.nzyme.core.dot11.anonymization.Anonymizer;
import app.nzyme.core.dot11.frames.*;
import app.nzyme.core.dot11.parsers.*;
import app.nzyme.core.notifications.Notification;
import app.nzyme.core.notifications.uplinks.misc.LoopbackUplink;
import app.nzyme.core.processing.FrameProcessor;
import org.pcap4j.packet.IllegalRawDataException;
import org.testng.annotations.Test;

import static org.testng.Assert.*;

public class BroadMonitorInterceptorSetTest extends InterceptorSetTest {

    @Test
    public void testDot11ProbeResponseFrame() throws MalformedFrameException, IllegalRawDataException {
        NzymeNode nzyme = new MockNzyme();
        LoopbackUplink loopback = new LoopbackUplink();
        nzyme.registerUplink(loopback);

        Dot11ProbeResponseFrame frame = new Dot11ProbeResponseFrameParser(new MetricRegistry(), new Anonymizer(false, ""))
                .parse(Frames.PROBE_RESP_1_PAYLOAD, Frames.PROBE_RESP_1_HEADER, META_NO_WEP);

        FrameProcessor processor = new FrameProcessor();
        processor.registerDot11Interceptors(new BroadMonitorInterceptorSet(nzyme).getInterceptors());
        processor.processDot11Frame(frame);

        Notification n = loopback.getLastNotification();

        assertEquals(n.getAdditionalFields().size(), 10);
        assertEquals(n.getMessage(), "b0:93:5b:1d:c8:f1 responded to probe request from 3c:8d:20:52:e4:87 for Home 5F48");
        assertEquals(n.getAdditionalFields().get("channel"), 1);
        assertEquals(n.getAdditionalFields().get("destination"), "3c:8d:20:52:e4:87");
        assertEquals(n.getAdditionalFields().get("transmitter"), "b0:93:5b:1d:c8:f1");
        assertEquals(n.getAdditionalFields().get("ssid"), "Home 5F48");
        assertEquals(n.getAdditionalFields().get("security_full"), "WPA2-PSK-CCMP");
        assertEquals(n.getAdditionalFields().get("is_wpa1"), false);
        assertEquals(n.getAdditionalFields().get("is_wpa2"), true);
        assertEquals(n.getAdditionalFields().get("is_wpa3"), false);
        assertEquals(n.getAdditionalFields().get("is_wps"), true);
        assertEquals(n.getAdditionalFields().get("subtype"), "probe-resp");
    }

    @Test
    public void testDot11ProbeResponseFrameWPA3() throws MalformedFrameException, IllegalRawDataException {
        NzymeNode nzyme = new MockNzyme();
        LoopbackUplink loopback = new LoopbackUplink();
        nzyme.registerUplink(loopback);

        Dot11ProbeResponseFrame frame = new Dot11ProbeResponseFrameParser(new MetricRegistry(), new Anonymizer(false, ""))
                .parse(Frames.PROBE_RESP_PSKSHA256_SAE_PAYLOAD, Frames.PROBE_RESP_PSKSHA256_SAE_HEADER, META_NO_WEP);

        FrameProcessor processor = new FrameProcessor();
        processor.registerDot11Interceptors(new BroadMonitorInterceptorSet(nzyme).getInterceptors());
        processor.processDot11Frame(frame);

        Notification n = loopback.getLastNotification();

        assertEquals(n.getAdditionalFields().size(), 10);
        assertEquals(n.getMessage(), "9c:ed:d5:fd:5b:2b responded to probe request from 46:78:72:9b:73:72 for wpa3test2");
        assertEquals(n.getAdditionalFields().get("channel"), 1);
        assertEquals(n.getAdditionalFields().get("destination"), "46:78:72:9b:73:72");
        assertEquals(n.getAdditionalFields().get("transmitter"), "9c:ed:d5:fd:5b:2b");
        assertEquals(n.getAdditionalFields().get("ssid"), "wpa3test2");
        assertEquals(n.getAdditionalFields().get("security_full"), "WPA3-PSK-PSKSHA256-SAE-CCMP");
        assertEquals(n.getAdditionalFields().get("is_wpa1"), false);
        assertEquals(n.getAdditionalFields().get("is_wpa2"), false);
        assertEquals(n.getAdditionalFields().get("is_wpa3"), true);
        assertEquals(n.getAdditionalFields().get("is_wps"), false);
        assertEquals(n.getAdditionalFields().get("subtype"), "probe-resp");
    }

    @Test
    public void testDot11ProbeReqFrame() throws MalformedFrameException, IllegalRawDataException {
        NzymeNode nzyme = new MockNzyme();
        LoopbackUplink loopback = new LoopbackUplink();
        nzyme.registerUplink(loopback);

        Dot11ProbeRequestFrame frame = new Dot11ProbeRequestFrameParser(new MetricRegistry(), new Anonymizer(false, ""))
                .parse(Frames.PROBE_REQ_1_PAYLOAD, Frames.PROBE_REQ_1_HEADER, META_NO_WEP);

        FrameProcessor processor = new FrameProcessor();
        processor.registerDot11Interceptors(new BroadMonitorInterceptorSet(nzyme).getInterceptors());
        processor.processDot11Frame(frame);

        Notification n = loopback.getLastNotification();

        assertEquals(n.getAdditionalFields().size(), 4);
        assertEquals(n.getMessage(), "Probe request: 3c:8d:20:25:20:e9 is looking for ATT6r8YXW9");
        assertEquals(n.getAdditionalFields().get("channel"), 1);
        assertEquals(n.getAdditionalFields().get("ssid"), "ATT6r8YXW9");
        assertEquals(n.getAdditionalFields().get("transmitter"), "3c:8d:20:25:20:e9");
        assertEquals(n.getAdditionalFields().get("subtype"), "probe-req");
    }

    @Test
    public void testDot11ProbeReqBroadcastFrame() throws MalformedFrameException, IllegalRawDataException {
        NzymeNode nzyme = new MockNzyme();
        LoopbackUplink loopback = new LoopbackUplink();
        nzyme.registerUplink(loopback);

        Dot11ProbeRequestFrame frame = new Dot11ProbeRequestFrameParser(new MetricRegistry(), new Anonymizer(false, ""))
                .parse(Frames.PROBE_REQ_BROADCAST_1_PAYLOAD, Frames.PROBE_REQ_BROADCAST_1_HEADER, META_NO_WEP);

        FrameProcessor processor = new FrameProcessor();
        processor.registerDot11Interceptors(new BroadMonitorInterceptorSet(nzyme).getInterceptors());
        processor.processDot11Frame(frame);

        Notification n = loopback.getLastNotification();

        assertEquals(n.getAdditionalFields().size(), 4);
        assertEquals(n.getMessage(), "Probe request: f8:da:0c:2e:af:1c is looking for any network. (null probe request)");
        assertEquals(n.getAdditionalFields().get("channel"), 1);
        assertEquals(n.getAdditionalFields().get("ssid"), "[no SSID]");
        assertEquals(n.getAdditionalFields().get("transmitter"), "f8:da:0c:2e:af:1c");
        assertEquals(n.getAdditionalFields().get("subtype"), "probe-req");
    }


    @Test
    public void testDot11DisassocFrame() throws MalformedFrameException, IllegalRawDataException {
        NzymeNode nzyme = new MockNzyme();
        LoopbackUplink loopback = new LoopbackUplink();
        nzyme.registerUplink(loopback);

        Dot11DisassociationFrame frame = new Dot11DisassociationFrameParser(new MetricRegistry(), new Anonymizer(false, ""))
                .parse(Frames.DISASSOC_1_PAYLOAD, Frames.DISASSOC_1_HEADER, META_NO_WEP);

        FrameProcessor processor = new FrameProcessor();
        processor.registerDot11Interceptors(new BroadMonitorInterceptorSet(nzyme).getInterceptors());
        processor.processDot11Frame(frame);

        Notification n = loopback.getLastNotification();

        assertEquals(n.getAdditionalFields().size(), 6);
        assertEquals(n.getMessage(), "b4:fb:e4:41:f6:45 is disassociating from b0:70:2d:56:1c:f7 (Disassociated because sending STA is leaving (or has left) BSS)");
        assertEquals(n.getAdditionalFields().get("channel"), 1);
        assertEquals(n.getAdditionalFields().get("transmitter"), "b4:fb:e4:41:f6:45");
        assertEquals(n.getAdditionalFields().get("destination"), "b0:70:2d:56:1c:f7");
        assertEquals(n.getAdditionalFields().get("reason_code"), (short)8);
        assertEquals(n.getAdditionalFields().get("reason_string"), "Disassociated because sending STA is leaving (or has left) BSS");
        assertEquals(n.getAdditionalFields().get("subtype"), "disassoc");
    }

    @Test
    public void testDot11DeauthFrame() throws MalformedFrameException, IllegalRawDataException {
        NzymeNode nzyme = new MockNzyme();
        LoopbackUplink loopback = new LoopbackUplink();
        nzyme.registerUplink(loopback);

        Dot11DeauthenticationFrame frame = new Dot11DeauthenticationFrameParser(new MetricRegistry(), new Anonymizer(false, ""))
                .parse(Frames.DEAUTH_1_PAYLOAD, Frames.DEAUTH_1_HEADER, META_NO_WEP);

        FrameProcessor processor = new FrameProcessor();
        processor.registerDot11Interceptors(new BroadMonitorInterceptorSet(nzyme).getInterceptors());
        processor.processDot11Frame(frame);

        Notification n = loopback.getLastNotification();

        assertEquals(n.getAdditionalFields().size(), 7);
        assertEquals(n.getMessage(), "Deauth: Transmitter b0:93:5b:1d:c8:f1 is deauthenticating e4:b2:fb:27:50:15 from BSSID b0:93:5b:1d:c8:f1 (Class 2 frame received from nonauthenticated STA)");
        assertEquals(n.getAdditionalFields().get("channel"), 1);
        assertEquals(n.getAdditionalFields().get("transmitter"), "b0:93:5b:1d:c8:f1");
        assertEquals(n.getAdditionalFields().get("destination"), "e4:b2:fb:27:50:15");
        assertEquals(n.getAdditionalFields().get("bssid"), "b0:93:5b:1d:c8:f1");
        assertEquals(n.getAdditionalFields().get("reason_code"), (short)6);
        assertEquals(n.getAdditionalFields().get("reason_string"), "Class 2 frame received from nonauthenticated STA");
        assertEquals(n.getAdditionalFields().get("subtype"), "deauth");
    }

    @Test
    public void testDot11BeaconFrame() throws MalformedFrameException, IllegalRawDataException {
        NzymeNode nzyme = new MockNzyme();
        LoopbackUplink loopback = new LoopbackUplink();
        nzyme.registerUplink(loopback);

        Dot11BeaconFrame frame = new Dot11BeaconFrameParser(new MetricRegistry(), new Anonymizer(false, ""))
                .parse(Frames.BEACON_1_PAYLOAD, Frames.BEACON_1_HEADER, META_NO_WEP);

        FrameProcessor processor = new FrameProcessor();
        processor.registerDot11Interceptors(new BroadMonitorInterceptorSet(nzyme).getInterceptors());
        processor.processDot11Frame(frame);

        Notification n = loopback.getLastNotification();

        assertEquals(n.getAdditionalFields().size(), 10);
        assertEquals(n.getMessage(), "Received beacon from 00:c0:ca:95:68:3b for SSID WTF");
        assertEquals(n.getAdditionalFields().get("channel"), 1);
        assertEquals(n.getAdditionalFields().get("transmitter"), "00:c0:ca:95:68:3b");
        assertEquals(n.getAdditionalFields().get("transmitter_fingerprint"), "dfac3abce0c722f9609343f7dfa208afa51a1c7decbd2eb6f96c78051f0a594b");
        assertEquals(n.getAdditionalFields().get("ssid"), "WTF");
        assertEquals(n.getAdditionalFields().get("security_full"), "WPA1-EAM-PSK-CCMP, WPA2-EAM-PSK-CCMP");
        assertEquals(n.getAdditionalFields().get("is_wpa1"), true);
        assertEquals(n.getAdditionalFields().get("is_wpa2"), true);
        assertEquals(n.getAdditionalFields().get("is_wpa3"), false);
        assertEquals(n.getAdditionalFields().get("is_wps"), false);
        assertEquals(n.getAdditionalFields().get("subtype"), "beacon");
    }

    @Test
    public void testDot11BeaconFrameBroadcast() throws MalformedFrameException, IllegalRawDataException {
        NzymeNode nzyme = new MockNzyme();
        LoopbackUplink loopback = new LoopbackUplink();
        nzyme.registerUplink(loopback);

        Dot11BeaconFrame frame = new Dot11BeaconFrameParser(new MetricRegistry(), new Anonymizer(false, ""))
                .parse(Frames.BEACON_4_PAYLOAD, Frames.BEACON_4_HEADER, META_NO_WEP);

        FrameProcessor processor = new FrameProcessor();
        processor.registerDot11Interceptors(new BroadMonitorInterceptorSet(nzyme).getInterceptors());
        processor.processDot11Frame(frame);

        Notification n = loopback.getLastNotification();

        assertEquals(n.getAdditionalFields().size(), 10);
        assertEquals(n.getMessage(), "Received broadcast beacon from 24:a4:3c:7d:01:cc");
        assertEquals(n.getAdditionalFields().get("channel"), 1);
        assertEquals(n.getAdditionalFields().get("transmitter"), "24:a4:3c:7d:01:cc");
        assertEquals(n.getAdditionalFields().get("transmitter_fingerprint"), "52f519b9e8b1a4901a3db02407ff62246f5cfc2d5ddadd5a10e5230524ef04a9");
        assertEquals(n.getAdditionalFields().get("ssid"), "[no SSID]");
        assertEquals(n.getAdditionalFields().get("security_full"), "NONE");
        assertEquals(n.getAdditionalFields().get("is_wpa1"), false);
        assertEquals(n.getAdditionalFields().get("is_wpa2"), false);
        assertEquals(n.getAdditionalFields().get("is_wpa3"), false);
        assertEquals(n.getAdditionalFields().get("is_wps"), false);
        assertEquals(n.getAdditionalFields().get("subtype"), "beacon");
    }

    @Test
    public void testDot11BeaconFrameWPA3() throws MalformedFrameException, IllegalRawDataException {
        NzymeNode nzyme = new MockNzyme();
        LoopbackUplink loopback = new LoopbackUplink();
        nzyme.registerUplink(loopback);

        Dot11BeaconFrame frame = new Dot11BeaconFrameParser(new MetricRegistry(), new Anonymizer(false, ""))
                .parse(Frames.BEACON_PSKSHA256_SAE_PAYLOAD, Frames.BEACON_PSKSHA256_SAE_HEADER, META_NO_WEP);

        FrameProcessor processor = new FrameProcessor();
        processor.registerDot11Interceptors(new BroadMonitorInterceptorSet(nzyme).getInterceptors());
        processor.processDot11Frame(frame);

        Notification n = loopback.getLastNotification();

        assertEquals(n.getAdditionalFields().size(), 10);
        assertEquals(n.getMessage(), "Received beacon from 9c:ed:d5:fd:5b:2b for SSID wpa3test2");
        assertEquals(n.getAdditionalFields().get("channel"), 1);
        assertEquals(n.getAdditionalFields().get("transmitter"), "9c:ed:d5:fd:5b:2b");
        assertEquals(n.getAdditionalFields().get("transmitter_fingerprint"), "b368c28c554e2f663a8c2c8704fd48d7e9189235e6f6e96024d77655d003b77e");
        assertEquals(n.getAdditionalFields().get("ssid"), "wpa3test2");
        assertEquals(n.getAdditionalFields().get("security_full"), "WPA3-PSK-PSKSHA256-SAE-CCMP");
        assertEquals(n.getAdditionalFields().get("is_wpa1"), false);
        assertEquals(n.getAdditionalFields().get("is_wpa2"), false);
        assertEquals(n.getAdditionalFields().get("is_wpa3"), true);
        assertEquals(n.getAdditionalFields().get("is_wps"), false);
        assertEquals(n.getAdditionalFields().get("subtype"), "beacon");
    }

    @Test
    public void testDot11AuthFrameOpenSystemSeq1() throws MalformedFrameException, IllegalRawDataException {
        NzymeNode nzyme = new MockNzyme();
        LoopbackUplink loopback = new LoopbackUplink();
        nzyme.registerUplink(loopback);

        Dot11AuthenticationFrame frame = new Dot11AuthenticationFrameParser(new MetricRegistry(), new Anonymizer(false, ""))
                .parse(Frames.AUTH_SUCCESS_STAGE_1_PAYLOAD, Frames.AUTH_SUCCESS_STAGE_1_HEADER, META_NO_WEP);

        FrameProcessor processor = new FrameProcessor();
        processor.registerDot11Interceptors(new BroadMonitorInterceptorSet(nzyme).getInterceptors());
        processor.processDot11Frame(frame);

        Notification n = loopback.getLastNotification();

        assertEquals(n.getAdditionalFields().size(), 6);
        assertEquals(n.getMessage(), "ac:5f:3e:b9:5d:be is requesting to authenticate with Open System (Open, WPA, WPA2, ...) at e0:22:03:f8:a3:39");
        assertEquals(n.getAdditionalFields().get("channel"), 1);
        assertEquals(n.getAdditionalFields().get("transmitter"), "ac:5f:3e:b9:5d:be");
        assertEquals(n.getAdditionalFields().get("destination"), "e0:22:03:f8:a3:39");
        assertEquals(n.getAdditionalFields().get("authentication_algorithm"), "open_system");
        assertEquals(n.getAdditionalFields().get("transaction_sequence_number"), (short) 1);
        assertEquals(n.getAdditionalFields().get("subtype"), "auth");
    }

    @Test
    public void testDot11AuthFrameOpenSystemSeq2() throws MalformedFrameException, IllegalRawDataException {
        NzymeNode nzyme = new MockNzyme();
        LoopbackUplink loopback = new LoopbackUplink();
        nzyme.registerUplink(loopback);

        Dot11AuthenticationFrame frame = new Dot11AuthenticationFrameParser(new MetricRegistry(), new Anonymizer(false, ""))
                .parse(Frames.AUTH_SUCCESS_STAGE_2_PAYLOAD, Frames.AUTH_SUCCESS_STAGE_2_HEADER, META_NO_WEP);

        FrameProcessor processor = new FrameProcessor();
        processor.registerDot11Interceptors(new BroadMonitorInterceptorSet(nzyme).getInterceptors());
        processor.processDot11Frame(frame);

        Notification n = loopback.getLastNotification();

        assertEquals(n.getAdditionalFields().size(), 8);
        assertEquals(n.getMessage(), "2c:5d:93:04:5c:09 is responding to Open System (Open, WPA, WPA2, ...) authentication request from 64:76:ba:d8:5d:ab. (success)");
        assertEquals(n.getAdditionalFields().get("channel"), 1);
        assertEquals(n.getAdditionalFields().get("transmitter"), "2c:5d:93:04:5c:09");
        assertEquals(n.getAdditionalFields().get("destination"), "64:76:ba:d8:5d:ab");
        assertEquals(n.getAdditionalFields().get("response_code"), (short) 0);
        assertEquals(n.getAdditionalFields().get("response_string"), "success");
        assertEquals(n.getAdditionalFields().get("authentication_algorithm"), "open_system");
        assertEquals(n.getAdditionalFields().get("transaction_sequence_number"), (short) 2);
        assertEquals(n.getAdditionalFields().get("subtype"), "auth");
    }

    @Test
    public void testDot11AuthFrameSharedKmySeq1() throws MalformedFrameException, IllegalRawDataException {
        NzymeNode nzyme = new MockNzyme();
        LoopbackUplink loopback = new LoopbackUplink();
        nzyme.registerUplink(loopback);

        Dot11AuthenticationFrame frame = new Dot11AuthenticationFrameParser(new MetricRegistry(), new Anonymizer(false, ""))
                .parse(Frames.AUTH_SUCCESS_WEP_STAGE_1_PAYLOAD, Frames.AUTH_SUCCESS_WEP_STAGE_1_HEADER, META_NO_WEP);

        FrameProcessor processor = new FrameProcessor();
        processor.registerDot11Interceptors(new BroadMonitorInterceptorSet(nzyme).getInterceptors());
        processor.processDot11Frame(frame);

        Notification n = loopback.getLastNotification();

        assertEquals(n.getAdditionalFields().size(), 6);
        assertEquals(n.getMessage(), "e0:33:8e:34:9e:73 is requesting to authenticate using WEP at f2:e5:6f:7c:84:6d");
        assertEquals(n.getAdditionalFields().get("channel"), 1);
        assertEquals(n.getAdditionalFields().get("transmitter"), "e0:33:8e:34:9e:73");
        assertEquals(n.getAdditionalFields().get("destination"), "f2:e5:6f:7c:84:6d");
        assertEquals(n.getAdditionalFields().get("authentication_algorithm"), "shared_key");
        assertEquals(n.getAdditionalFields().get("transaction_sequence_number"), (short) 1);
        assertEquals(n.getAdditionalFields().get("subtype"), "auth");
    }

    @Test
    public void testDot11AuthFrameSharedKmySeq2() throws MalformedFrameException, IllegalRawDataException {
        NzymeNode nzyme = new MockNzyme();
        LoopbackUplink loopback = new LoopbackUplink();
        nzyme.registerUplink(loopback);

        Dot11AuthenticationFrame frame = new Dot11AuthenticationFrameParser(new MetricRegistry(), new Anonymizer(false, ""))
                .parse(Frames.AUTH_SUCCESS_WEP_STAGE_2_PAYLOAD, Frames.AUTH_SUCCESS_WEP_STAGE_2_HEADER, META_NO_WEP);

        FrameProcessor processor = new FrameProcessor();
        processor.registerDot11Interceptors(new BroadMonitorInterceptorSet(nzyme).getInterceptors());
        processor.processDot11Frame(frame);

        Notification n = loopback.getLastNotification();

        assertEquals(n.getAdditionalFields().size(), 6);
        assertEquals(n.getMessage(), "f2:e5:6f:7c:84:6d is responding to WEP authentication request at e0:33:8e:34:9e:73 with clear text challenge.");
        assertEquals(n.getAdditionalFields().get("channel"), 1);
        assertEquals(n.getAdditionalFields().get("transmitter"), "f2:e5:6f:7c:84:6d");
        assertEquals(n.getAdditionalFields().get("destination"), "e0:33:8e:34:9e:73");
        assertEquals(n.getAdditionalFields().get("authentication_algorithm"), "shared_key");
        assertEquals(n.getAdditionalFields().get("transaction_sequence_number"), (short) 2);
        assertEquals(n.getAdditionalFields().get("subtype"), "auth");
    }

    @Test
    public void testDot11AuthFrameSharedKmySeq4() throws MalformedFrameException, IllegalRawDataException {
        NzymeNode nzyme = new MockNzyme();
        LoopbackUplink loopback = new LoopbackUplink();
        nzyme.registerUplink(loopback);

        Dot11AuthenticationFrame frame = new Dot11AuthenticationFrameParser(new MetricRegistry(), new Anonymizer(false, ""))
                .parse(Frames.AUTH_SUCCESS_WEP_STAGE_4_PAYLOAD, Frames.AUTH_SUCCESS_WEP_STAGE_4_HEADER, META_NO_WEP);

        FrameProcessor processor = new FrameProcessor();
        processor.registerDot11Interceptors(new BroadMonitorInterceptorSet(nzyme).getInterceptors());
        processor.processDot11Frame(frame);

        Notification n = loopback.getLastNotification();

        assertEquals(n.getAdditionalFields().size(), 8);
        assertEquals(n.getMessage(), "f2:e5:6f:7c:84:6d is responding to WEP authentication request from e0:33:8e:34:9e:73. (success)");
        assertEquals(n.getAdditionalFields().get("channel"), 1);
        assertEquals(n.getAdditionalFields().get("transmitter"), "f2:e5:6f:7c:84:6d");
        assertEquals(n.getAdditionalFields().get("destination"), "e0:33:8e:34:9e:73");
        assertEquals(n.getAdditionalFields().get("response_code"), (short) 0);
        assertEquals(n.getAdditionalFields().get("response_string"), "success");
        assertEquals(n.getAdditionalFields().get("authentication_algorithm"), "shared_key");
        assertEquals(n.getAdditionalFields().get("transaction_sequence_number"), (short) 4);
        assertEquals(n.getAdditionalFields().get("subtype"), "auth");
    }

    @Test
    public void testDot11AuthFrameSharedKmySeq4Failure() throws MalformedFrameException, IllegalRawDataException {
        NzymeNode nzyme = new MockNzyme();
        LoopbackUplink loopback = new LoopbackUplink();
        nzyme.registerUplink(loopback);

        Dot11AuthenticationFrame frame = new Dot11AuthenticationFrameParser(new MetricRegistry(), new Anonymizer(false, ""))
                .parse(Frames.AUTH_FAILED_WEP_STAGE_4_PAYLOAD, Frames.AUTH_FAILED_WEP_STAGE_4_HEADER, META_NO_WEP);

        FrameProcessor processor = new FrameProcessor();
        processor.registerDot11Interceptors(new BroadMonitorInterceptorSet(nzyme).getInterceptors());
        processor.processDot11Frame(frame);

        Notification n = loopback.getLastNotification();

        assertEquals(n.getAdditionalFields().size(), 8);
        assertEquals(n.getMessage(), "f2:e5:6f:7c:84:6d is responding to WEP authentication request from e0:33:8e:34:9e:73. (failure)");
        assertEquals(n.getAdditionalFields().get("channel"), 1);
        assertEquals(n.getAdditionalFields().get("transmitter"), "f2:e5:6f:7c:84:6d");
        assertEquals(n.getAdditionalFields().get("destination"), "e0:33:8e:34:9e:73");
        assertEquals(n.getAdditionalFields().get("response_code"), (short) 1);
        assertEquals(n.getAdditionalFields().get("response_string"), "failure");
        assertEquals(n.getAdditionalFields().get("authentication_algorithm"), "shared_key");
        assertEquals(n.getAdditionalFields().get("transaction_sequence_number"), (short) 4);
        assertEquals(n.getAdditionalFields().get("subtype"), "auth");
    }

    @Test
    public void testDot11AuthFrameWPA3CommitSeqSuccess() throws MalformedFrameException, IllegalRawDataException {
        NzymeNode nzyme = new MockNzyme();
        LoopbackUplink loopback = new LoopbackUplink();
        nzyme.registerUplink(loopback);

        Dot11AuthenticationFrame frame = new Dot11AuthenticationFrameParser(new MetricRegistry(), new Anonymizer(false, ""))
                .parse(Frames.AUTH_SUCCESS_COMMIT_PSKSHA256_SAE_PAYLOAD, Frames.AUTH_SUCCESS_COMMIT_PSKSHA256_SAE_HEADER, META_NO_WEP);

        FrameProcessor processor = new FrameProcessor();
        processor.registerDot11Interceptors(new BroadMonitorInterceptorSet(nzyme).getInterceptors());
        processor.processDot11Frame(frame);

        Notification n = loopback.getLastNotification();

        assertEquals(n.getAdditionalFields().size(), 6);
        assertEquals(n.getMessage(), "2a:e9:1e:d5:11:2a is requesting to authenticate using SAE (WPA3) at 9c:ed:d5:fd:5b:2b");
        assertEquals(n.getAdditionalFields().get("channel"), 1);
        assertEquals(n.getAdditionalFields().get("transmitter"), "2a:e9:1e:d5:11:2a");
        assertEquals(n.getAdditionalFields().get("destination"), "9c:ed:d5:fd:5b:2b");
        assertEquals(n.getAdditionalFields().get("authentication_algorithm"), "sae");
        assertEquals(n.getAdditionalFields().get("transaction_sequence_number"), (short) 1);
        assertEquals(n.getAdditionalFields().get("subtype"), "auth");
    }

    @Test
    public void testDot11AuthFrameWPA3ConfirmSeqSuccess() throws MalformedFrameException, IllegalRawDataException {
        NzymeNode nzyme = new MockNzyme();
        LoopbackUplink loopback = new LoopbackUplink();
        nzyme.registerUplink(loopback);

        Dot11AuthenticationFrame frame = new Dot11AuthenticationFrameParser(new MetricRegistry(), new Anonymizer(false, ""))
                .parse(Frames.AUTH_SUCCESS_CONFIRM_PSKSHA256_SAE_PAYLOAD, Frames.AUTH_SUCCESS_CONFIRM_PSKSHA256_SAE_HEADER, META_NO_WEP);

        FrameProcessor processor = new FrameProcessor();
        processor.registerDot11Interceptors(new BroadMonitorInterceptorSet(nzyme).getInterceptors());
        processor.processDot11Frame(frame);

        Notification n = loopback.getLastNotification();

        assertEquals(n.getAdditionalFields().size(), 8);
        assertEquals(n.getMessage(), "9c:ed:d5:fd:5b:2b is responding to SAE (WPA3) authentication request from 2a:e9:1e:d5:11:2a. (success)");
        assertEquals(n.getAdditionalFields().get("channel"), 1);
        assertEquals(n.getAdditionalFields().get("transmitter"), "9c:ed:d5:fd:5b:2b");
        assertEquals(n.getAdditionalFields().get("destination"), "2a:e9:1e:d5:11:2a");
        assertEquals(n.getAdditionalFields().get("response_code"), (short) 0);
        assertEquals(n.getAdditionalFields().get("response_string"), "success");
        assertEquals(n.getAdditionalFields().get("authentication_algorithm"), "sae");
        assertEquals(n.getAdditionalFields().get("transaction_sequence_number"), (short) 2);
        assertEquals(n.getAdditionalFields().get("subtype"), "auth");
    }

    @Test
    public void testDot11AuthFrameWPA3ConfirmSeqFailure() throws MalformedFrameException, IllegalRawDataException {
        NzymeNode nzyme = new MockNzyme();
        LoopbackUplink loopback = new LoopbackUplink();
        nzyme.registerUplink(loopback);

        Dot11AuthenticationFrame frame = new Dot11AuthenticationFrameParser(new MetricRegistry(), new Anonymizer(false, ""))
                .parse(Frames.AUTH_SUCCESS_CONFIRM_PSKSHA256_SAE_FAILURE_PAYLOAD, Frames.AUTH_SUCCESS_CONFIRM_PSKSHA256_SAE_FAILURE_HEADER, META_NO_WEP);

        FrameProcessor processor = new FrameProcessor();
        processor.registerDot11Interceptors(new BroadMonitorInterceptorSet(nzyme).getInterceptors());
        processor.processDot11Frame(frame);

        Notification n = loopback.getLastNotification();

        assertEquals(n.getAdditionalFields().size(), 8);
        assertEquals(n.getMessage(), "9c:ed:d5:fd:5b:2b is responding to SAE (WPA3) authentication request from 2a:e9:1e:d5:11:2a. (failure)");
        assertEquals(n.getAdditionalFields().get("channel"), 1);
        assertEquals(n.getAdditionalFields().get("transmitter"), "9c:ed:d5:fd:5b:2b");
        assertEquals(n.getAdditionalFields().get("destination"), "2a:e9:1e:d5:11:2a");
        assertEquals(n.getAdditionalFields().get("response_code"), (short) 1);
        assertEquals(n.getAdditionalFields().get("response_string"), "failure");
        assertEquals(n.getAdditionalFields().get("authentication_algorithm"), "sae");
        assertEquals(n.getAdditionalFields().get("transaction_sequence_number"), (short) 2);
        assertEquals(n.getAdditionalFields().get("subtype"), "auth");
    }

    @Test
    public void testDot11AssocResponseSuccessResponse() throws MalformedFrameException, IllegalRawDataException {
        NzymeNode nzyme = new MockNzyme();
        LoopbackUplink loopback = new LoopbackUplink();
        nzyme.registerUplink(loopback);

        Dot11AssociationResponseFrame frame = new Dot11AssociationResponseFrameParser(new MetricRegistry(), new Anonymizer(false, ""))
                .parse(Frames.ASSOC_RESP_SUCCESS_1_PAYLOAD, Frames.ASSOC_RESP_SUCCESS_1_HEADER, META_NO_WEP);

        FrameProcessor processor = new FrameProcessor();
        processor.registerDot11Interceptors(new BroadMonitorInterceptorSet(nzyme).getInterceptors());
        processor.processDot11Frame(frame);

        Notification n = loopback.getLastNotification();

        assertEquals(n.getAdditionalFields().size(), 6);
        assertEquals(n.getMessage(),"88:96:4e:4d:77:80 answered association request from 5c:77:76:d3:26:45. Response: SUCCESS (0)");
        assertEquals(n.getAdditionalFields().get("channel"), 1);
        assertEquals(n.getAdditionalFields().get("transmitter"), "88:96:4e:4d:77:80");
        assertEquals(n.getAdditionalFields().get("destination"), "5c:77:76:d3:26:45");
        assertEquals(n.getAdditionalFields().get("response_code"), (short)0);
        assertEquals(n.getAdditionalFields().get("response_string"), "success");
        assertEquals(n.getAdditionalFields().get("subtype"), "assoc-resp");
    }

    @Test
    public void testDot11AssocResponseFailResponse() throws MalformedFrameException, IllegalRawDataException {
        NzymeNode nzyme = new MockNzyme();
        LoopbackUplink loopback = new LoopbackUplink();
        nzyme.registerUplink(loopback);

        Dot11AssociationResponseFrame frame = new Dot11AssociationResponseFrameParser(new MetricRegistry(), new Anonymizer(false, ""))
                .parse(Frames.ASSOC_RESP_FAILED_1_PAYLOAD, Frames.ASSOC_RESP_FAILED_1_HEADER, META_NO_WEP);

        FrameProcessor processor = new FrameProcessor();
        processor.registerDot11Interceptors(new BroadMonitorInterceptorSet(nzyme).getInterceptors());
        processor.processDot11Frame(frame);

        Notification n = loopback.getLastNotification();

        assertEquals(n.getAdditionalFields().size(), 6);
        assertEquals(n.getMessage(),"88:96:4e:4d:77:80 answered association request from 5c:77:76:d3:26:45. Response: REFUSED (1)");
        assertEquals(n.getAdditionalFields().get("channel"), 1);
        assertEquals(n.getAdditionalFields().get("transmitter"), "88:96:4e:4d:77:80");
        assertEquals(n.getAdditionalFields().get("destination"), "5c:77:76:d3:26:45");
        assertEquals(n.getAdditionalFields().get("response_code"), (short)1);
        assertEquals(n.getAdditionalFields().get("response_string"), "refused");
        assertEquals(n.getAdditionalFields().get("subtype"), "assoc-resp");
    }

    @Test
    public void testHandleDot11AssocReqFrame() throws MalformedFrameException, IllegalRawDataException {
        NzymeNode nzyme = new MockNzyme();
        LoopbackUplink loopback = new LoopbackUplink();
        nzyme.registerUplink(loopback);

        Dot11AssociationRequestFrame frame = new Dot11AssociationRequestFrameParser(new MetricRegistry(), new Anonymizer(false, ""))
                .parse(Frames.ASSOC_REQ_1_PAYLOAD, Frames.ASSOC_REQ_1_HEADER, META_NO_WEP);

        FrameProcessor processor = new FrameProcessor();
        processor.registerDot11Interceptors(new BroadMonitorInterceptorSet(nzyme).getInterceptors());
        processor.processDot11Frame(frame);

        Notification n = loopback.getLastNotification();

        assertEquals(n.getAdditionalFields().size(), 5);
        assertEquals(n.getMessage(), "ac:81:12:d2:26:7e is requesting to associate with ATT4Q5FBC3 at 14:ed:bb:79:97:4d");
        assertEquals(n.getAdditionalFields().get("channel"), 1);
        assertEquals(n.getAdditionalFields().get("transmitter"), "ac:81:12:d2:26:7e");
        assertEquals(n.getAdditionalFields().get("destination"), "14:ed:bb:79:97:4d");
        assertEquals(n.getAdditionalFields().get("ssid"), "ATT4Q5FBC3");
        assertEquals(n.getAdditionalFields().get("subtype"), "assoc-req");
    }

    @Test
    public void testDot11NetworksAndClientsProcessing() throws MalformedFrameException, IllegalRawDataException {
        MockNzyme nzyme = new MockNzyme();
        LoopbackUplink loopback = new LoopbackUplink();
        nzyme.registerUplink(loopback);


        FrameProcessor processor = new FrameProcessor();
        processor.registerDot11Interceptors(new BroadMonitorInterceptorSet(nzyme).getInterceptors());

        processor.processDot11Frame(new Dot11BeaconFrameParser(new MetricRegistry(), new Anonymizer(false, "")).parse(
                Frames.BEACON_1_PAYLOAD, Frames.BEACON_1_HEADER, META_NO_WEP
        ));

        processor.processDot11Frame(new Dot11ProbeResponseFrameParser(new MetricRegistry(), new Anonymizer(false, "")).parse(
                Frames.PROBE_RESP_1_PAYLOAD, Frames.PROBE_RESP_1_HEADER, META_NO_WEP
        ));

        processor.processDot11Frame(new Dot11ProbeRequestFrameParser(new MetricRegistry(), new Anonymizer(false, "")).parse(
                Frames.PROBE_REQ_1_PAYLOAD, Frames.PROBE_REQ_1_HEADER, META_NO_WEP
        ));

        processor.processDot11Frame(new Dot11AssociationRequestFrameParser(new MetricRegistry(), new Anonymizer(false, "")).parse(
                Frames.ASSOC_REQ_1_PAYLOAD, Frames.ASSOC_REQ_1_HEADER, META_NO_WEP
        ));

        assertEquals(nzyme.getNetworks().getSSIDs().size(), 2);

        assertEquals(nzyme.getNetworks().getBSSIDs().size(), 2);
        assertNotNull(nzyme.getNetworks().getBSSIDs().get("b0:93:5b:1d:c8:f1"));
        assertNotNull(nzyme.getNetworks().getBSSIDs().get("00:c0:ca:95:68:3b"));

        assertEquals(nzyme.getClients().getClients().size(), 2);
        assertNotNull(nzyme.getClients().getClients().get("3c:8d:20:25:20:e9"));
        assertNotNull(nzyme.getClients().getClients().get("ac:81:12:d2:26:7e"));

        // assert getNetworks() getClients()
    }

}
