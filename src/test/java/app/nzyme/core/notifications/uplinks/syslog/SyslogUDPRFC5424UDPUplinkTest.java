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

package app.nzyme.core.notifications.uplinks.syslog;

import app.nzyme.core.dot11.Dot11MetaInformation;
import app.nzyme.core.notifications.Notification;
import org.testng.annotations.Test;

import java.net.InetSocketAddress;

import static org.testng.Assert.*;

public class SyslogUDPRFC5424UDPUplinkTest {

    protected static final Dot11MetaInformation META_NO_WEP = new Dot11MetaInformation(false, 100, 2400, 1, 0L, false);

    @Test
    public void testBuildMessage() {
        SyslogUDPRFC5424UDPUplink up = new SyslogUDPRFC5424UDPUplink(InetSocketAddress.createUnresolved("127.0.0.1", 9001), "foo");
        String msg = up.buildMessage(new Notification("Received beacon from 82:2a:a8:0c:01:a2 for SSID Flancrest-Enterprises (2417MHz @ -1dBm)", 11), META_NO_WEP);
        assertEquals(msg, "Received beacon from 82:2a:a8:0c:01:a2 for SSID Flancrest-Enterprises (2417MHz @ -1dBm) (2400MHz @ 100dBm) channel=\"11\" nzyme_sensor_id=\"foo\" nzyme_message_type=\"frame_record\" antenna_signal=\"100\" frequency=\"2400\" signal_quality=\"100\" mac_timestamp=\"0\"");
    }

    @Test
    public void testBuildMessageNoMeta() {
        SyslogUDPRFC5424UDPUplink up = new SyslogUDPRFC5424UDPUplink(InetSocketAddress.createUnresolved("127.0.0.1", 9001), "foo");
        String msg = up.buildMessage(new Notification("Received beacon from 82:2a:a8:0c:01:a2 for SSID Flancrest-Enterprises (2417MHz @ -1dBm)", 11), null);
        assertEquals(msg, "Received beacon from 82:2a:a8:0c:01:a2 for SSID Flancrest-Enterprises (2417MHz @ -1dBm) channel=\"11\" nzyme_sensor_id=\"foo\" nzyme_message_type=\"frame_record\"");
    }

}