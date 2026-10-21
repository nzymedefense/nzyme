package app.nzyme.core.ethernet.beacons;

import com.google.auto.value.AutoValue;

import java.util.Set;

@AutoValue
public abstract class BeaconsConfiguration {

    public abstract boolean detectionEnabled();
    public abstract Set<BeaconType> enabledSources();
    public abstract boolean includeSiteLocalDestinations();
    public abstract int detectionWindowMinutes();
    public abstract int minimumCheckins();
    public abstract int minimumIntervalSeconds();
    public abstract int dnsMinimumIntervalSeconds();
    public abstract int maximumJitterPercent();
    public abstract int minimumRegularityPercent();
    public abstract int checkinRetentionTimeDays();
    public abstract boolean alertOnNewBeacon();
    public abstract boolean alertOnNewSource();
    public abstract boolean alertOnSuspiciousActivity();
    public abstract boolean alertOnMaliciousActivity();

    public static BeaconsConfiguration create(boolean detectionEnabled, Set<BeaconType> enabledSources, boolean includeSiteLocalDestinations, int detectionWindowMinutes, int minimumCheckins, int minimumIntervalSeconds, int dnsMinimumIntervalSeconds, int maximumJitterPercent, int minimumRegularityPercent, int checkinRetentionTimeDays, boolean alertOnNewBeacon, boolean alertOnNewSource, boolean alertOnSuspiciousActivity, boolean alertOnMaliciousActivity) {
        return builder()
                .detectionEnabled(detectionEnabled)
                .enabledSources(enabledSources)
                .includeSiteLocalDestinations(includeSiteLocalDestinations)
                .detectionWindowMinutes(detectionWindowMinutes)
                .minimumCheckins(minimumCheckins)
                .minimumIntervalSeconds(minimumIntervalSeconds)
                .dnsMinimumIntervalSeconds(dnsMinimumIntervalSeconds)
                .maximumJitterPercent(maximumJitterPercent)
                .minimumRegularityPercent(minimumRegularityPercent)
                .checkinRetentionTimeDays(checkinRetentionTimeDays)
                .alertOnNewBeacon(alertOnNewBeacon)
                .alertOnNewSource(alertOnNewSource)
                .alertOnSuspiciousActivity(alertOnSuspiciousActivity)
                .alertOnMaliciousActivity(alertOnMaliciousActivity)
                .build();
    }

    public static Builder builder() {
        return new AutoValue_BeaconsConfiguration.Builder();
    }

    @AutoValue.Builder
    public abstract static class Builder {
        public abstract Builder detectionEnabled(boolean detectionEnabled);

        public abstract Builder enabledSources(Set<BeaconType> enabledSources);

        public abstract Builder includeSiteLocalDestinations(boolean includeSiteLocalDestinations);

        public abstract Builder detectionWindowMinutes(int detectionWindowMinutes);

        public abstract Builder minimumCheckins(int minimumCheckins);

        public abstract Builder minimumIntervalSeconds(int minimumIntervalSeconds);

        public abstract Builder dnsMinimumIntervalSeconds(int dnsMinimumIntervalSeconds);

        public abstract Builder maximumJitterPercent(int maximumJitterPercent);

        public abstract Builder minimumRegularityPercent(int minimumRegularityPercent);

        public abstract Builder checkinRetentionTimeDays(int checkinRetentionTimeDays);

        public abstract Builder alertOnNewBeacon(boolean alertOnNewBeacon);

        public abstract Builder alertOnNewSource(boolean alertOnNewSource);

        public abstract Builder alertOnSuspiciousActivity(boolean alertOnSuspiciousActivity);

        public abstract Builder alertOnMaliciousActivity(boolean alertOnMaliciousActivity);

        public abstract BeaconsConfiguration build();
    }
}
