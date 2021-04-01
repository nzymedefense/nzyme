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

package horse.wtf.nzyme.notifications.uplinks.syslog;

import com.cloudbees.syslog.Facility;
import com.cloudbees.syslog.MessageFormat;
import com.cloudbees.syslog.sender.UdpSyslogMessageSender;
import horse.wtf.nzyme.alerts.Alert;
import horse.wtf.nzyme.dot11.Dot11MetaInformation;
import horse.wtf.nzyme.notifications.FieldNames;
import horse.wtf.nzyme.notifications.Notification;
import horse.wtf.nzyme.notifications.Uplink;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.Nullable;

import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.util.Map;

public abstract class SyslogUDPUplink implements Uplink {

    private static final Logger LOG = LogManager.getLogger(SyslogUDPUplink.class);

    protected final UdpSyslogMessageSender sender;
    private final String nzymeId;

    protected SyslogUDPUplink(InetSocketAddress address, MessageFormat messageFormat, String nzymeId) {
        this.nzymeId = nzymeId;

        sender = new UdpSyslogMessageSender();
        sender.setMessageFormat(messageFormat);
        sender.setSyslogServerHostname(address.getHostName());
        sender.setSyslogServerPort(address.getPort());

        String hostname;
        try {
            hostname = InetAddress.getLocalHost().getCanonicalHostName();
        } catch (Exception e) {
            hostname = "nzyme";
            LOG.error("Could not determine local hostname for syslog uplink. Falling back to [{}].", hostname, e);
        }

        sender.setDefaultMessageHostname(hostname);
        sender.setDefaultAppName("nzyme");
        sender.setDefaultFacility(Facility.USER);
    }


    @Override
    public void notify(Notification notification, @Nullable Dot11MetaInformation meta) {
        try {
            sender.sendMessage(buildMessage(notification, meta));
        } catch (Exception e) {
            LOG.info("Could not send syslog message.", e);
        }
    }

    @Override
    public void notifyOfAlert(Alert alert) {
        try {
            StringBuilder sb = new StringBuilder("ALERT: ");
            sb.append(alert.getMessage());

            for (Map.Entry<String, Object> field : alert.getFields().entrySet()) {
                sb.append(" ").append(field.getKey()).append("=\"").append(field.getValue().toString().replace("\"", "\\\"")).append("\"");
            }

            sb.append(" ").append(FieldNames.NZYME_SENSOR_ID).append("=\"").append(this.nzymeId).append("\"");
            sb.append(" ").append(FieldNames.NZYME_MESSAGE_TYPE).append("=\"alert\"");
            sb.append(" ").append(FieldNames.ALERT_TYPE).append("=\"").append(alert.getType().toString().toLowerCase()).append("\"");


            sender.sendMessage(sb.toString());
        } catch (Exception e) {
            LOG.info("Could not send syslog message.", e);
        }
    }

    public String buildMessage(Notification notification, @Nullable Dot11MetaInformation meta) {
        StringBuilder sb = new StringBuilder(notification.getMessage());

        if(meta != null) {
            sb.append(" ").append("(").append(meta.getFrequency()).append("MHz @")
                    .append(" ").append(meta.getAntennaSignal()).append("dBm)");
        }

        for (Map.Entry<String, Object> field : notification.getAdditionalFields().entrySet()) {
            sb.append(" ").append(field.getKey()).append("=\"").append(field.getValue().toString().replace("\"", "\\\"")).append("\"");
        }

        sb.append(" ").append(FieldNames.NZYME_SENSOR_ID).append("=\"").append(this.nzymeId).append("\"");
        sb.append(" ").append(FieldNames.NZYME_MESSAGE_TYPE).append("=\"frame_record\"");

        if(meta != null) {
            sb.append(" ").append(FieldNames.ANTENNA_SIGNAL).append("=\"").append(meta.getAntennaSignal()).append("\"");
            sb.append(" ").append(FieldNames.FREQUENCY).append("=\"").append(meta.getFrequency()).append("\"");
            sb.append(" ").append(FieldNames.SIGNAL_QUALITY).append("=\"").append(meta.getSignalQuality()).append("\"");

            if(meta.getMacTimestamp() >= 0) {
                sb.append(" ").append(FieldNames.MAC_TIMESTAMP).append("=\"").append(meta.getMacTimestamp()).append("\"");
            }
        }

        return sb.toString();
    }

}
