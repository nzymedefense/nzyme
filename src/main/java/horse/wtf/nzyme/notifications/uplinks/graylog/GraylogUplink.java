/*
 *  This file is part of Nzyme.
 *
 *  Nzyme is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  Nzyme is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with Nzyme.  If not, see <http://www.gnu.org/licenses/>.
 */

package horse.wtf.nzyme.notifications.uplinks.graylog;

import horse.wtf.nzyme.dot11.Dot11MetaInformation;
import horse.wtf.nzyme.notifications.Notification;
import horse.wtf.nzyme.notifications.Uplink;
import org.graylog2.gelfclient.GelfConfiguration;
import org.graylog2.gelfclient.GelfMessage;
import org.graylog2.gelfclient.GelfTransports;
import org.graylog2.gelfclient.transport.GelfTransport;

import javax.annotation.Nullable;
import java.net.InetSocketAddress;
import java.net.InetAddress;
import java.net.UnknownHostException;

import static horse.wtf.nzyme.Tools.calculateSignalQuality;

public class GraylogUplink implements Uplink {

    private final String source;

    private final String nzymeId;
    private final String networkInterfaceName;

    private final GelfTransport gelfTransport;

    public GraylogUplink(String hostname, int port, String nzymeId, String networkInterfaceName) {
        this.nzymeId = nzymeId;
        this.networkInterfaceName = networkInterfaceName;
	
	String tmpsrc;

        try {
                tmpsrc = InetAddress.getLocalHost().getHostName();
        } catch(UnknownHostException e) {
                tmpsrc = "nzyme";
        }

	source = tmpsrc;

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

        GelfMessage gelf = new GelfMessage(sb.toString(), source);
        gelf.addAdditionalFields(notification.getAdditionalFields());
        gelf.addAdditionalField("nzyme_sensor_id", this.nzymeId);
        gelf.addAdditionalField("nic_name", this.networkInterfaceName);

        // Meta information.
        if(meta != null) {
            gelf.addAdditionalField("signal_strength", meta.getAntennaSignal());
            gelf.addAdditionalField("frequency", meta.getFrequency());
            gelf.addAdditionalField("signal_quality", calculateSignalQuality(meta.getAntennaSignal()));
            gelf.addAdditionalField("is_wep", meta.isWep());

            if(meta.getMacTimestamp() >= 0) {
                gelf.addAdditionalField("mac_timestamp", meta.getMacTimestamp());
            }
        }

        this.gelfTransport.trySend(gelf);
    }

}
