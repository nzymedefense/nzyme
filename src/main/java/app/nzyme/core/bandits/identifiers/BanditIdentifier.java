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

package app.nzyme.core.bandits.identifiers;

import app.nzyme.core.dot11.frames.Dot11BeaconFrame;
import app.nzyme.core.dot11.frames.Dot11DeauthenticationFrame;
import app.nzyme.core.dot11.frames.Dot11ProbeResponseFrame;

import java.util.Map;
import java.util.Optional;
import java.util.UUID;

public abstract class BanditIdentifier {

    public enum TYPE {
        FINGERPRINT,
        SSID,
        SIGNAL_STRENGTH,
        PWNAGOTCHI_IDENTITY
    }

    public abstract BanditIdentifierDescriptor descriptor();
    public abstract Map<String, Object> configuration();

    public abstract Optional<Boolean> matches(Dot11DeauthenticationFrame frame);
    public abstract Optional<Boolean> matches(Dot11BeaconFrame frame);
    public abstract Optional<Boolean> matches(Dot11ProbeResponseFrame frame);

    private final Long databaseID;
    private final UUID uuid;
    private final TYPE type;

    public BanditIdentifier(Long databaseID, UUID uuid, TYPE type) {
        this.databaseID = databaseID;
        this.uuid = uuid;
        this.type = type;
    }

    public TYPE getType() {
        return type;
    }

    public long getDatabaseID() {
        return databaseID;
    }

    public UUID getUuid() {
        return uuid;
    }

}
