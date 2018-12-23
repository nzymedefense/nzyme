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
import horse.wtf.nzyme.dot11.probes.Dot11Probe;
import org.joda.time.DateTime;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.AtomicReference;

public abstract class Alert {

    public enum Type {
        UNEXPECTED_BEACON_BSSID,
        UNEXPECTED_PROBERESP_BSSID,
        UNEXPECTED_BEACON_SSID,
        UNEXPECTED_PROBERESP_SSID
    }

    private final Subsystem subsystem;
    private final Map<String, Object> fields;
    private final DateTime firstSeen;
    private final AtomicReference<DateTime> lastSeen;
    private final AtomicLong frameCount;
    private final Dot11Probe probe;

    private final String description;
    private final String documentationLink;
    private final List<String> falsePositives;

    public abstract String getMessage();
    public abstract Type getType();
    public abstract boolean sameAs(Alert alert);

    protected UUID uuid;

    protected Alert(DateTime timestamp, Subsystem subsystem, Map<String, Object> fields, String description, String documentationLink, List<String> falsePositives, Dot11Probe probe) {
        this.firstSeen = timestamp;
        this.lastSeen = new AtomicReference<>(timestamp);
        this.subsystem = subsystem;
        this.fields = ImmutableMap.copyOf(fields);
        this.description = description;
        this.documentationLink = documentationLink;
        this.falsePositives = falsePositives;
        this.probe = probe;

        this.frameCount = new AtomicLong(1);
    }

    public DateTime getFirstSeen() {
        return firstSeen;
    }

    public DateTime getLastSeen() {
        return lastSeen.get();
    }

    public void setLastSeen(DateTime timestamp) {
        this.lastSeen.set(timestamp);
    }

    public void incrementFrameCount() {
        this.frameCount.incrementAndGet();
    }

    public long getFrameCount() {
        return this.frameCount.get();
    }

    public Subsystem getSubsystem() {
        return subsystem;
    }

    public Map<String, Object> getFields() {
        return fields;
    }

    public String getDescription() {
        return description;
    }

    public String getDocumentationLink() {
        return documentationLink;
    }

    public List<String> getFalsePositives() {
        return falsePositives;
    }

    public Dot11Probe getProbe() {
        return probe;
    }

    public UUID getUUID() {
        return uuid;
    }

    public void setUUID(UUID uuid) {
        this.uuid = uuid;
    }

}
