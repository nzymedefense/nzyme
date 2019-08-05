package horse.wtf.nzyme.alerts;

import com.google.common.base.Strings;
import com.google.common.collect.ImmutableMap;
import horse.wtf.nzyme.Subsystem;
import horse.wtf.nzyme.notifications.FieldNames;
import org.joda.time.DateTime;

import javax.validation.constraints.NotNull;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class SignalStrengthAnomalyAlert extends Alert {

    private static final String DESCRIPTION = "One of our networks is showing signal strength anomalies. A possible attacker " +
            "might be running a rogue device impersonating our network from a different physical location than the legitimate " +
            "access point and cause this signal strength anomaly.";
    private static final String DOC_LINK = "guidance-SIGNAL_ANOMALY";
    private static final List<String> FALSE_POSITIVES = new ArrayList<String>(){{
        add("The location or TX power of an access point has been changed. The system will learn the new signal strength baseline.");
        add("Changes in the environment and RF spectrum can lead to rapid and frequent changes in the signal strength and trigger " +
                "this alert. Consult the nzyme documentation and fine-tune thresholds to avoid alerting on normal signal strength fluctuations.");
        add("Literally sun spots.");
    }};

    private SignalStrengthAnomalyAlert(DateTime timestamp, Subsystem subsystem, Map<String, Object> fields) {
        super(timestamp, subsystem, fields, DESCRIPTION, DOC_LINK, FALSE_POSITIVES, null, false);
    }

    @Override
    public String getMessage() {
        return "Signal strength anomaly detected for our SSID [" + getSSID() + "] on [" + getBSSID() + "] channel <" + getChannel() + ">.";
    }

    @Override
    public Type getType() {
        return Type.SIGNAL_ANOMALY;
    }

    public String getSSID() {
        return (String) getFields().get(FieldNames.SSID);
    }

    public String getBSSID() {
        return (String) getFields().get(FieldNames.BSSID);
    }

    public int getChannel() {
        return (int) getFields().get(FieldNames.CHANNEL);
    }

    @Override
    public boolean sameAs(Alert alert) {
        if (!(alert instanceof SignalStrengthAnomalyAlert)) {
            return false;
        }

        SignalStrengthAnomalyAlert a = (SignalStrengthAnomalyAlert) alert;

        return a.getSSID().equals(this.getSSID()) && a.getBSSID().equals(this.getBSSID());
    }

    public static SignalStrengthAnomalyAlert create(@NotNull String ssid, String bssid, int channel) {
        if (Strings.isNullOrEmpty(ssid)) {
            throw new IllegalArgumentException("This alert cannot be raised for hidden/broadcast SSIDs.");
        }

        ImmutableMap.Builder<String, Object> fields = new ImmutableMap.Builder<>();
        fields.put(FieldNames.SSID, ssid);
        fields.put(FieldNames.BSSID, bssid.toLowerCase());
        fields.put(FieldNames.CHANNEL, channel);

        return new SignalStrengthAnomalyAlert(DateTime.now(), Subsystem.DOT_11, fields.build());
    }

}
