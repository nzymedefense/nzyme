package app.nzyme.core.rest.responses.uav;

import com.google.auto.value.AutoValue;
import jakarta.annotation.Nullable;
import org.joda.time.DateTime;

import java.util.UUID;

@AutoValue
public abstract class UavDetailsResponse {

    public abstract long id();
    public abstract UUID tapUuid();
    public abstract String identifier();
    public abstract UavType uavType();
    public abstract UavDetectionSource detectionSource();
    public abstract double rssiAverage();
    @Nullable
    public abstract UavOperationalStatus operationalStatus();
    @Nullable
    public abstract Double latitude();
    @Nullable
    public abstract Double longitude();
    @Nullable
    public abstract Integer groundTrack();
    @Nullable
    public abstract Double speed();
    @Nullable
    public abstract Double verticalSpeed();
    @Nullable
    public abstract Double altitudePressure();
    @Nullable
    public abstract Double altitudeGeodetic();
    @Nullable
    public abstract UavHeightType heightType();
    @Nullable
    public abstract Double height();
    @Nullable
    public abstract Integer accuracyHorizontal();
    @Nullable
    public abstract Integer accuracyVertical();
    @Nullable
    public abstract Integer accuracyBarometer();
    @Nullable
    public abstract Integer accuracySpeed();
    @Nullable
    public abstract UavOperatorLocationType operatorLocationType();
    @Nullable
    public abstract Double operatorLatitude();
    @Nullable
    public abstract Double operatorLongitude();
    @Nullable
    public abstract Double operatorAltitude();
    public abstract DateTime firstSeen();
    public abstract DateTime lastSeen();


}
