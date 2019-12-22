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

package horse.wtf.nzyme.alerts;

import com.google.common.collect.ImmutableMap;
import horse.wtf.nzyme.Subsystem;
import horse.wtf.nzyme.dot11.Dot11MetaInformation;
import horse.wtf.nzyme.dot11.interceptors.misc.PwnagotchiAdvertisement;
import horse.wtf.nzyme.dot11.probes.Dot11Probe;
import horse.wtf.nzyme.notifications.FieldNames;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.joda.time.DateTime;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class PwnagotchiAdvertisementAlert extends Alert {

    private static final Logger LOG = LogManager.getLogger(PwnagotchiAdvertisementAlert.class);

    private static final String DESCRIPTION = "A pwnagotchi beacon advertisement frame was detected. The pwnagotchi is a " +
            "popular and mostly automated WiFi attack platform. It uses a parasitic protocol embedded in 802.11 beacon frames " +
            "to advertise it's own existence to other pwnagotchis in range and nzyme is able to detect those advertisement frames.";
    private static final String DOC_LINK = "guidance-PWNAGOTCHI_ADVERTISEMENT";
    private static final List<String> FALSE_POSITIVES = new ArrayList<String>(){{
        add("No known false positives.");
    }};

    private PwnagotchiAdvertisementAlert(DateTime timestamp, Subsystem subsystem, Map<String, Object> fields, long frameCount) {
        super(timestamp, subsystem, fields, DESCRIPTION, DOC_LINK, FALSE_POSITIVES, true, frameCount);
    }

    @Override
    public String getMessage() {
        return "Pwnagotchi [" + getName() + "] with identity [" + getIdentity() + "] and version [" + getVersion() + "] detected. Uptime [" + getUptime() + "].";
    }

    @Override
    public TYPE getType() {
        return TYPE.PWNAGOTCHI_ADVERTISEMENT;
    }

    public String getName() {
        return (String) getFields().get(FieldNames.NAME);
    }

    public String getVersion() {
        return (String) getFields().get(FieldNames.VERSION);
    }

    public String getIdentity() {
        return (String) getFields().get(FieldNames.IDENTITY);
    }

    public double getUptime() {
        return (double) getFields().get(FieldNames.UPTIME);
    }

    @Override
    public boolean sameAs(Alert alert) {
        if (!(alert instanceof PwnagotchiAdvertisementAlert)) {
            return false;
        }

        PwnagotchiAdvertisementAlert a = (PwnagotchiAdvertisementAlert) alert;

        return a.getIdentity().equals(this.getIdentity());
    }

    public static PwnagotchiAdvertisementAlert create(DateTime firstSeen, PwnagotchiAdvertisement advertisement, int channel, int frequency, int antennaSignal, long frameCount) {
        ImmutableMap.Builder<String, Object> fields = new ImmutableMap.Builder<>();

        fields.put(FieldNames.NAME, advertisement.name() == null ? "unknown" : advertisement.name());
        fields.put(FieldNames.VERSION, advertisement.version() == null ? "0" : advertisement.version());
        fields.put(FieldNames.IDENTITY, advertisement.identity() == null ? "unknown" : advertisement.identity());
        fields.put(FieldNames.UPTIME, advertisement.uptime() == null ? -1 : advertisement.uptime());
        fields.put(FieldNames.PWND_THIS_RUN, advertisement.pwndThisRun() == null ? -1 : advertisement.pwndThisRun());
        fields.put(FieldNames.PWND_TOTAL, advertisement.pwndTotal() == null ? -1 : advertisement.pwndTotal());
        fields.put(FieldNames.CHANNEL, channel);
        fields.put(FieldNames.FREQUENCY, frequency);
        fields.put(FieldNames.ANTENNA_SIGNAL, antennaSignal);

        return new PwnagotchiAdvertisementAlert(firstSeen, Subsystem.DOT_11, fields.build(), frameCount);
    }

}