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

package horse.wtf.nzyme.bandits.engine;

import com.codahale.metrics.MetricRegistry;
import com.codahale.metrics.Timer;
import com.google.auto.value.AutoValue;
import com.google.common.base.Strings;
import horse.wtf.nzyme.bandits.Identifiable;
import horse.wtf.nzyme.bandits.identifiers.BanditIdentifier;
import horse.wtf.nzyme.dot11.frames.Dot11BeaconFrame;
import horse.wtf.nzyme.dot11.frames.Dot11DeauthenticationFrame;
import horse.wtf.nzyme.dot11.frames.Dot11Frame;
import horse.wtf.nzyme.dot11.frames.Dot11ProbeResponseFrame;
import horse.wtf.nzyme.util.MetricNames;

import java.util.Optional;

public class ContactIdentifierEngine {

    private final Timer timing;

    public ContactIdentifierEngine(MetricRegistry metrics) {
        this.timing = metrics.timer(MetricNames.CONTACT_IDENTIFIER_TIMING);
    }

    public Optional<ContactIdentification> identify(Dot11Frame frame, Identifiable bandit) {
        if (bandit.identifiers() == null || bandit.identifiers().isEmpty()) {
            return Optional.empty();
        }

        Timer.Context timer = this.timing.time();
        try {
            for (BanditIdentifier identifier : bandit.identifiers()) {
                if (frame instanceof Dot11BeaconFrame) {
                    Dot11BeaconFrame beacon = (Dot11BeaconFrame) frame;
                    Optional<Boolean> matches = identifier.matches(beacon);
                    if (matches.isPresent() && matches.get()) {
                        if (Strings.isNullOrEmpty(beacon.ssid())) {
                            // Broadcast.
                            return Optional.of(ContactIdentification.create( beacon.transmitter(), Optional.empty()));
                        } else {
                            return Optional.of(ContactIdentification.create(beacon.transmitter(), Optional.of(beacon.ssid())));
                        }
                    }
                }

                if (frame instanceof Dot11ProbeResponseFrame) {
                    Dot11ProbeResponseFrame probeResponse = (Dot11ProbeResponseFrame) frame;
                    Optional<Boolean> matches = identifier.matches(probeResponse);
                    if (matches.isPresent() && matches.get()) {
                        if (Strings.isNullOrEmpty(probeResponse.ssid())) {
                            // Broadcast.
                            return Optional.of(ContactIdentification.create(probeResponse.transmitter(), Optional.empty()));
                        } else {
                            return Optional.of(ContactIdentification.create(probeResponse.transmitter(), Optional.of(probeResponse.ssid())));
                        }
                    }
                }

                if (frame instanceof Dot11DeauthenticationFrame) {
                    Dot11DeauthenticationFrame deauth = (Dot11DeauthenticationFrame) frame;
                    Optional<Boolean> matches = identifier.matches(deauth);
                    if (matches.isPresent() && matches.get()) {
                        return Optional.of(ContactIdentification.create(deauth.transmitter(), Optional.empty()));
                    }
                }
            }
        } finally {
            timer.stop();
        }

        return Optional.empty();
    }

    @AutoValue
    public static abstract class ContactIdentification {

        public abstract String bssid();
        public abstract Optional<String> ssid();

        public static ContactIdentification create(String bssid, Optional<String> ssid) {
            return builder()
                    .bssid(bssid)
                    .ssid(ssid)
                    .build();
        }

        public static Builder builder() {
            return new AutoValue_ContactIdentifierEngine_ContactIdentification.Builder();
        }

        @AutoValue.Builder
        public abstract static class Builder {
            public abstract Builder bssid(String bssid);

            public abstract Builder ssid(Optional<String> ssid);

            public abstract ContactIdentification build();
        }

    }

}
