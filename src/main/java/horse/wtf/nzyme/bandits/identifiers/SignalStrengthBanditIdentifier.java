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

public class SignalStrengthBanditIdentifier extends BanditIdentifier {

    private final int from;
    private final int to;

    public SignalStrengthBanditIdentifier(int from, int to, Long databaseId, UUID uuid) {
        super(databaseId, uuid, TYPE.SIGNAL_STRENGTH);

        if (from > 0 || to < -100 || from <= to) {
            throw new IllegalArgumentException();
        }

        this.from = from;
        this.to = to;
    }

    @Override
    public Map<String, Object> configuration() {
        return new HashMap<String, Object>(){{
            put(FieldNames.FROM, from);
            put(FieldNames.TO, to);
        }};
    }

    @Override
    public BanditIdentifierDescriptor descriptor() {
        return BanditIdentifierDescriptor.create(
                TYPE.SIGNAL_STRENGTH,
                "Matches if the frame signal strength is within expected range.",
                "(frame.signal_quality >= " + to + " AND frame.signal_quality <= " + from + ")"
        );
    }

    @Override
    public Optional<Boolean> matches(Dot11DeauthenticationFrame frame) {
        return match(frame.meta().getAntennaSignal());
    }

    @Override
    public Optional<Boolean> matches(Dot11BeaconFrame frame) {
        return match(frame.meta().getAntennaSignal());
    }

    @Override
    public Optional<Boolean> matches(Dot11ProbeResponseFrame frame) {
        return match(frame.meta().getAntennaSignal());
    }

    private Optional<Boolean> match(int signalStrength) {
        return Optional.of(signalStrength >= to && signalStrength <= from);
    }

}
