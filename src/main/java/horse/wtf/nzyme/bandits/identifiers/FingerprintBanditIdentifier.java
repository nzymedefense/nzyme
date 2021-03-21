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

package horse.wtf.nzyme.bandits.identifiers;

import horse.wtf.nzyme.dot11.frames.Dot11BeaconFrame;
import horse.wtf.nzyme.dot11.frames.Dot11DeauthenticationFrame;
import horse.wtf.nzyme.dot11.frames.Dot11ProbeResponseFrame;
import horse.wtf.nzyme.notifications.FieldNames;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

public class FingerprintBanditIdentifier extends BanditIdentifier {

    private final String fingerprint;

    public FingerprintBanditIdentifier(String fingerprint, Long databaseID, UUID uuid) {
        super(databaseID, uuid, TYPE.FINGERPRINT);

        this.fingerprint = fingerprint;
    }

    public String getFingerprint() {
        return fingerprint;
    }

    @Override
    public BanditIdentifierDescriptor descriptor() {
        return BanditIdentifierDescriptor.create(
                TYPE.FINGERPRINT,
                "Matches if the frame fingerprint equals the expected fingerprint.",
                "frame.fingerprint == \"" + fingerprint + "\""
        );
    }

    @Override
    public Map<String, Object> configuration() {
        return new HashMap<String, Object>(){{
            put(FieldNames.FINGERPRINT, fingerprint);
        }};
    }

    @Override
    public Optional<Boolean> matches(Dot11DeauthenticationFrame frame) {
        return Optional.empty();
    }

    @Override
    public Optional<Boolean> matches(Dot11BeaconFrame frame) {
        return match(frame.transmitterFingerprint());
    }

    @Override
    public Optional<Boolean> matches(Dot11ProbeResponseFrame frame) {
        return match(frame.transmitterFingerprint());
    }

    private Optional<Boolean> match(String receivedFingerprint) {
        if(receivedFingerprint == null) {
            return Optional.of(false);
        }

        return Optional.of(receivedFingerprint.equals(fingerprint));
    }

}
