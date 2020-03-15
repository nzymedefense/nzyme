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

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.base.Splitter;
import com.google.common.collect.ImmutableMap;
import horse.wtf.nzyme.Subsystem;
import horse.wtf.nzyme.alerts.service.AlertDatabaseEntry;
import horse.wtf.nzyme.alerts.service.AlertsService;
import horse.wtf.nzyme.dot11.interceptors.misc.PwnagotchiAdvertisement;
import horse.wtf.nzyme.notifications.FieldNames;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.joda.time.DateTime;

import javax.annotation.Nullable;
import java.io.IOException;
import java.lang.reflect.Field;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.AtomicReference;

public abstract class Alert {
    
    public enum TYPE_WIDE {
        UNEXPECTED_BSSID,
        UNEXPECTED_SSID,
        CRYPTO_CHANGE,
        UNEXPECTED_CHANNEL,
        KNOWN_BANDIT_FINGERPRINT,
        UNEXPECTED_FINGERPRINT,
        SIGNAL_ANOMALY,
        BEACON_RATE_ANOMALY,
        PROBE_RESPONSE_TRAP_1,
        MULTIPLE_SIGNAL_TRACKS,
        PWNAGOTCHI_ADVERTISEMENT
    }

    public enum TYPE {
        UNEXPECTED_BSSID_BEACON,
        UNEXPECTED_BSSID_PROBERESP,
        UNEXPECTED_SSID_BEACON,
        UNEXPECTED_SSID_PROBERESP,
        CRYPTO_CHANGE_BEACON,
        CRYPTO_CHANGE_PROBERESP,
        UNEXPECTED_CHANNEL_BEACON,
        UNEXPECTED_CHANNEL_PROBERESP,
        KNOWN_BANDIT_FINGERPRINT_BEACON,
        KNOWN_BANDIT_FINGERPRINT_PROBERESP,
        UNEXPECTED_FINGERPRINT_BEACON,
        UNEXPECTED_FINGERPRINT_PROBERESP,
        BEACON_RATE_ANOMALY,
        PROBE_RESPONSE_TRAP_1,
        MULTIPLE_SIGNAL_TRACKS,
        PWNAGOTCHI_ADVERTISEMENT,
        BANDIT_CONTACT
    }

    private final Subsystem subsystem;
    private final Map<String, Object> fields;
    private final DateTime firstSeen;
    private final AtomicReference<DateTime> lastSeen;
    private final AtomicLong frameCount;
    private final boolean useFrameCount;

    private final String description;
    private final String documentationLink;
    private final List<String> falsePositives;

    public abstract String getMessage();
    public abstract TYPE getType();
    public abstract boolean sameAs(Alert alert);

    protected UUID uuid;

    protected Alert(DateTime timestamp,
                    Subsystem subsystem,
                    Map<String, Object> fields,
                    String description,
                    String documentationLink,
                    List<String> falsePositives,
                    boolean useFrameCount,
                    long frameCount) {
        this.firstSeen = timestamp;
        this.lastSeen = new AtomicReference<>(timestamp);
        this.subsystem = subsystem;
        this.fields = ImmutableMap.copyOf(fields);
        this.description = description;
        this.documentationLink = documentationLink;
        this.falsePositives = falsePositives;
        this.useFrameCount = useFrameCount;

        this.frameCount = new AtomicLong(frameCount);
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

    @Nullable
    public Long getFrameCount() {
        return this.useFrameCount ? this.frameCount.get() : null;
    }

    public boolean isUseFrameCount() {
        return this.useFrameCount;
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

    public UUID getUUID() {
        return uuid;
    }

    public void setUUID(UUID uuid) {
        this.uuid = uuid;
    }

    public static Alert serializeFromDatabase(AlertDatabaseEntry db) throws IOException {
        ObjectMapper om = new ObjectMapper();
        Map<String, Object> fields = om.readValue(db.fields(), new TypeReference<Map<String, Object>>(){});

        Alert alert;
        switch (db.type()) {
            case UNEXPECTED_BSSID_BEACON:
                alert = UnexpectedBSSIDBeaconAlert.create(
                        db.firstSeen(),
                        (String) fields.get(FieldNames.SSID),
                        (String) fields.get(FieldNames.BSSID),
                        (Integer) fields.get(FieldNames.CHANNEL),
                        (Integer) fields.get(FieldNames.FREQUENCY),
                        (Integer) fields.get(FieldNames.ANTENNA_SIGNAL),
                        db.frameCount()
                );
                break;
            case UNEXPECTED_BSSID_PROBERESP:
                alert = UnexpectedBSSIDProbeRespAlert.create(
                        db.firstSeen(),
                        (String) fields.get(FieldNames.SSID),
                        (String) fields.get(FieldNames.BSSID),
                        (String) fields.get(FieldNames.DESTINATION),
                        (Integer) fields.get(FieldNames.CHANNEL),
                        (Integer) fields.get(FieldNames.FREQUENCY),
                        (Integer) fields.get(FieldNames.ANTENNA_SIGNAL),
                        db.frameCount()
                );
                break;
            case UNEXPECTED_SSID_BEACON:
                alert = UnexpectedSSIDBeaconAlert.create(
                        db.firstSeen(),
                        (String) fields.get(FieldNames.SSID),
                        (String) fields.get(FieldNames.BSSID),
                        (Integer) fields.get(FieldNames.CHANNEL),
                        (Integer) fields.get(FieldNames.FREQUENCY),
                        (Integer) fields.get(FieldNames.ANTENNA_SIGNAL),
                        db.frameCount()
                );
                break;
            case UNEXPECTED_SSID_PROBERESP:
                alert = UnexpectedSSIDProbeRespAlert.create(
                        db.firstSeen(),
                        (String) fields.get(FieldNames.SSID),
                        (String) fields.get(FieldNames.BSSID),
                        (Integer) fields.get(FieldNames.CHANNEL),
                        (Integer) fields.get(FieldNames.FREQUENCY),
                        (Integer) fields.get(FieldNames.ANTENNA_SIGNAL),
                        db.frameCount()
                );
                break;
            case CRYPTO_CHANGE_BEACON:
                alert = CryptoChangeBeaconAlert.create(
                        db.firstSeen(),
                        (String) fields.get(FieldNames.SSID),
                        (String) fields.get(FieldNames.BSSID),
                        (String) fields.get(FieldNames.ENCOUNTERED_SECURITY),
                        (Integer) fields.get(FieldNames.CHANNEL),
                        (Integer) fields.get(FieldNames.FREQUENCY),
                        (Integer) fields.get(FieldNames.ANTENNA_SIGNAL),
                        db.frameCount()
                );
                break;
            case CRYPTO_CHANGE_PROBERESP:
                alert = CryptoChangeProbeRespAlert.create(
                        db.firstSeen(),
                        (String) fields.get(FieldNames.SSID),
                        (String) fields.get(FieldNames.BSSID),
                        (String) fields.get(FieldNames.ENCOUNTERED_SECURITY),
                        (Integer) fields.get(FieldNames.CHANNEL),
                        (Integer) fields.get(FieldNames.FREQUENCY),
                        (Integer) fields.get(FieldNames.ANTENNA_SIGNAL),
                        db.frameCount()
                );
                break;
            case UNEXPECTED_CHANNEL_BEACON:
                alert = UnexpectedChannelBeaconAlert.create(
                        db.firstSeen(),
                        (String) fields.get(FieldNames.SSID),
                        (String) fields.get(FieldNames.BSSID),
                        (Integer) fields.get(FieldNames.CHANNEL),
                        (Integer) fields.get(FieldNames.FREQUENCY),
                        (Integer) fields.get(FieldNames.ANTENNA_SIGNAL),
                        db.frameCount()
                );
                break;
            case UNEXPECTED_CHANNEL_PROBERESP:
                alert = UnexpectedChannelProbeRespAlert.create(
                        db.firstSeen(),
                        (String) fields.get(FieldNames.SSID),
                        (String) fields.get(FieldNames.BSSID),
                        (Integer) fields.get(FieldNames.CHANNEL),
                        (Integer) fields.get(FieldNames.FREQUENCY),
                        (Integer) fields.get(FieldNames.ANTENNA_SIGNAL),
                        db.frameCount()
                );
                break;
            case KNOWN_BANDIT_FINGERPRINT_BEACON:
                alert = KnownBanditFingerprintBeaconAlert.create(
                        db.firstSeen(),
                        Splitter.on(",").splitToList((String) fields.get(FieldNames.BANDIT_NAMES)),
                        (String) fields.get(FieldNames.BANDIT_FINGERPRINT),
                        (String) fields.get(FieldNames.SSID),
                        (String) fields.get(FieldNames.BSSID),
                        (Integer) fields.get(FieldNames.CHANNEL),
                        (Integer) fields.get(FieldNames.FREQUENCY),
                        (Integer) fields.get(FieldNames.ANTENNA_SIGNAL),
                        db.frameCount()
                );
                break;
            case KNOWN_BANDIT_FINGERPRINT_PROBERESP:
                alert = KnownBanditFingerprintProbeRespAlert.create(
                        db.firstSeen(),
                        Splitter.on(",").splitToList((String) fields.get(FieldNames.BANDIT_NAMES)),
                        (String) fields.get(FieldNames.BANDIT_FINGERPRINT),
                        (String) fields.get(FieldNames.SSID),
                        (String) fields.get(FieldNames.BSSID),
                        (Integer) fields.get(FieldNames.CHANNEL),
                        (Integer) fields.get(FieldNames.FREQUENCY),
                        (Integer) fields.get(FieldNames.ANTENNA_SIGNAL),
                        db.frameCount()
                );
                break;
            case UNEXPECTED_FINGERPRINT_BEACON:
                alert = UnexpectedFingerprintBeaconAlert.create(
                        db.firstSeen(),
                        (String) fields.get(FieldNames.SSID),
                        (String) fields.get(FieldNames.BANDIT_FINGERPRINT),
                        (String) fields.get(FieldNames.BSSID),
                        (Integer) fields.get(FieldNames.CHANNEL),
                        (Integer) fields.get(FieldNames.FREQUENCY),
                        (Integer) fields.get(FieldNames.ANTENNA_SIGNAL),
                        db.frameCount()
                );
                break;
            case UNEXPECTED_FINGERPRINT_PROBERESP:
                alert = UnexpectedFingerprintProbeRespAlert.create(
                        db.firstSeen(),
                        (String) fields.get(FieldNames.SSID),
                        (String) fields.get(FieldNames.BANDIT_FINGERPRINT),
                        (String) fields.get(FieldNames.BSSID),
                        (Integer) fields.get(FieldNames.CHANNEL),
                        (Integer) fields.get(FieldNames.FREQUENCY),
                        (Integer) fields.get(FieldNames.ANTENNA_SIGNAL),
                        db.frameCount()
                );
                break;
            case BEACON_RATE_ANOMALY:
                alert = BeaconRateAnomalyAlert.create(
                        db.firstSeen(),
                        (String) fields.get(FieldNames.SSID),
                        (String) fields.get(FieldNames.BSSID),
                        (Double) fields.get(FieldNames.BEACON_RATE),
                        (Integer) fields.get(FieldNames.BEACON_RATE_THRESHOLD)
                );
                break;
            case PROBE_RESPONSE_TRAP_1:
                throw new RuntimeException("NOT IMPLEMENTED.");
            case MULTIPLE_SIGNAL_TRACKS:
                alert = MultipleTrackAlert.create(
                        db.firstSeen(),
                        (String) fields.get(FieldNames.SSID),
                        (String) fields.get(FieldNames.BSSID),
                        (Integer) fields.get(FieldNames.CHANNEL),
                        (Integer) fields.get(FieldNames.TRACK_COUNT)
                );
                break;
            case PWNAGOTCHI_ADVERTISEMENT:
                alert = PwnagotchiAdvertisementAlert.create(
                        db.firstSeen(),
                        PwnagotchiAdvertisement.create(
                                (String) fields.get(FieldNames.NAME),
                                (String) fields.get(FieldNames.VERSION),
                                (String) fields.get(FieldNames.IDENTITY),
                                (Double) fields.get(FieldNames.UPTIME),
                                (Integer) fields.get(FieldNames.PWND_THIS_RUN),
                                (Integer) fields.get(FieldNames.PWND_TOTAL)
                        ),
                        (Integer) fields.get(FieldNames.CHANNEL),
                        (Integer) fields.get(FieldNames.FREQUENCY),
                        (Integer) fields.get(FieldNames.ANTENNA_SIGNAL),
                        db.frameCount()
                );
                break;
            case BANDIT_CONTACT:
                alert = BanditContactAlert.create(
                        db.firstSeen(),
                        (String) fields.get(FieldNames.BANDIT_NAME),
                        (String) fields.get(FieldNames.BANDIT_UUID),
                        db.frameCount()
                );
                break;
            default:
                throw new RuntimeException("Cannot serialize persisted alert of type [" + db.type() + "].");
        }

        alert.setLastSeen(db.lastSeen());
        alert.setUUID(db.uuid());

        return alert;
    }

}
