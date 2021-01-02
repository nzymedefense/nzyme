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

import com.google.common.base.Joiner;
import horse.wtf.nzyme.dot11.frames.Dot11BeaconFrame;
import horse.wtf.nzyme.dot11.frames.Dot11DeauthenticationFrame;
import horse.wtf.nzyme.dot11.frames.Dot11ProbeResponseFrame;
import horse.wtf.nzyme.notifications.FieldNames;

import java.util.*;
import java.util.stream.Collectors;

public class SSIDIBanditdentifier extends BanditIdentifier {

    private final List<String> ssids;
    private final String listDescription;

    public SSIDIBanditdentifier(List<String> ssids, Long databaseID, UUID uuid) {
        super(databaseID, uuid, TYPE.SSID);

        this.ssids = ssids;
        this.listDescription = ssids.stream()
                .map(s -> "\"" + s + "\"" )
                .collect(Collectors.joining(","));
    }

    @Override
    public BanditIdentifierDescriptor descriptor() {
        return BanditIdentifierDescriptor.create(
                TYPE.SSID,
                "Matches if the SSID advertised by frame is one of the configured SSIDs. (multiple SSIDs can be entered, separated by comma)",
                "frame.ssid IN [" + listDescription + "]"
        );
    }

    @Override
    public Map<String, Object> configuration() {
        return new HashMap<String, Object>(){{
            put(FieldNames.SSIDS, ssids);
        }};
    }

    @Override
    public Optional<Boolean> matches(Dot11DeauthenticationFrame frame) {
        return Optional.empty();
    }

    @Override
    public Optional<Boolean> matches(Dot11BeaconFrame frame) {
        return match(frame.ssid());
    }

    @Override
    public Optional<Boolean> matches(Dot11ProbeResponseFrame frame) {
        return match(frame.ssid());
    }

    private Optional<Boolean> match(String ssid) {
        return Optional.of(ssids.contains(ssid));
    }

}
