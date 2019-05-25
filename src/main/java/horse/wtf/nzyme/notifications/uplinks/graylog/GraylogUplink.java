/*
 *  This file is part of nzyme.
 *
 *  nzyme is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  nzyme is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with nzyme.  If not, see <http://www.gnu.org/licenses/>.
 */

package horse.wtf.nzyme.notifications.uplinks.graylog;

import horse.wtf.nzyme.alerts.Alert;
import horse.wtf.nzyme.dot11.Dot11MetaInformation;
import horse.wtf.nzyme.notifications.FieldNames;
import horse.wtf.nzyme.notifications.Notification;
import horse.wtf.nzyme.notifications.Uplink;
import org.graylog2.gelfclient.GelfConfiguration;
import org.graylog2.gelfclient.GelfMessage;
import org.graylog2.gelfclient.GelfTransports;
import org.graylog2.gelfclient.transport.GelfTransport;

import javax.annotation.Nullable;
import java.net.InetSocketAddress;
import java.util.Map;

public class GraylogUplink implements Uplink {

    private static final String SOURCE = "nzyme";

    private final String nzymeId;
    private final String networkInterfaceName;

    private final GelfTransport gelfTransport;

    public GraylogUplink(String hostname, int port, String nzymeId, String networkInterfaceName) {
        this.nzymeId = nzymeId;
        this.networkInterfaceName = networkInterfaceName;

        this.gelfTransport = GelfTransports.create(new GelfConfiguration(new InetSocketAddress(hostname, port))
                .transport(GelfTransports.TCP)
                .queueSize(512)
                .connectTimeout(5000)
                .reconnectDelay(1000)
                .tcpNoDelay(true)
                .sendBufferSize(32768));
    }

    @Override
    public void notify(Notification notification, @Nullable Dot11MetaInformation meta) {
        // Add signal strength and frequency to message.
        StringBuilder sb = new StringBuilder(notification.getMessage());

        if(meta != null) {
            sb.append(" ").append("(").append(meta.getFrequency()).append("MHz @")
                    .append(" ").append(meta.getAntennaSignal()).append("dBm)")
                    .toString();
        }

        GelfMessage gelf = new GelfMessage(sb.toString(), SOURCE);
        gelf.addAdditionalFields(notification.getAdditionalFields());
        gelf.addAdditionalField(FieldNames.NZYME_SENSOR_ID, this.nzymeId);
        gelf.addAdditionalField(FieldNames.NZYME_MESSAGE_TYPE, "frame_record");
        gelf.addAdditionalField(FieldNames.NZYME_NIC_NAME, this.networkInterfaceName);
        gelf.addAdditionalField(FieldNames.NZYME_PROBE_NAME, notification.getProbe().getName());

        // Meta information.
        if(meta != null) {
            gelf.addAdditionalField(FieldNames.ANTENNA_SIGNAL, meta.getAntennaSignal());
            gelf.addAdditionalField(FieldNames.FREQUENCY, meta.getFrequency());
            gelf.addAdditionalField(FieldNames.SIGNAL_QUALITY, meta.getSignalQuality());

            if(meta.getMacTimestamp() >= 0) {
                gelf.addAdditionalField(FieldNames.MAC_TIMESTAMP, meta.getMacTimestamp());
            }
        }

        this.gelfTransport.trySend(gelf);
    }

    @Override
    public void notifyOfAlert(Alert alert) {
        GelfMessage gelf = new GelfMessage("ALERT: " + alert.getMessage(), SOURCE);
        gelf.addAdditionalField(FieldNames.NZYME_SENSOR_ID, this.nzymeId);
        gelf.addAdditionalField(FieldNames.NZYME_MESSAGE_TYPE, "alert");
        gelf.addAdditionalField(FieldNames.NZYME_NIC_NAME, this.networkInterfaceName);
        gelf.addAdditionalField(FieldNames.NZYME_PROBE_NAME, alert.getProbe().getName());
        gelf.addAdditionalField(FieldNames.ALERT_TYPE, alert.getType().toString().toLowerCase());

        for (Map.Entry<String, Object> x : alert.getFields().entrySet()) {
            gelf.addAdditionalField("alert_" + x.getKey(), x.getValue());
        }

        this.gelfTransport.trySend(gelf);
    }

}
