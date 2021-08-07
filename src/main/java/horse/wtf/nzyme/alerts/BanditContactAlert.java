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

package horse.wtf.nzyme.alerts;

import com.google.common.base.Strings;
import com.google.common.collect.ImmutableMap;
import horse.wtf.nzyme.Subsystem;
import horse.wtf.nzyme.notifications.FieldNames;
import org.joda.time.DateTime;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public class BanditContactAlert extends Alert {

    private static final String DESCRIPTION = "Nzyme detected a bandit contact.";
    private static final String DOC_LINK = "guidance-BANDIT_CONTACT";
    private static final List<String> FALSE_POSITIVES = new ArrayList<String>(){{
        add("A mis-configured or too wide bandit definition could trigger an invalid contact.");
    }};

    private BanditContactAlert(DateTime timestamp, Subsystem subsystem, Map<String, Object> fields, long frameCount) {
        super(timestamp, subsystem, fields, DESCRIPTION, DOC_LINK, FALSE_POSITIVES, true, frameCount);
    }

    @Override
    public String getMessage() {
        if (getFields().containsKey(FieldNames.SSID)) {
            return "Bandit [" + getBanditName() + "] detected, advertising [" + getFields().get(FieldNames.SSID) + "].";
        } else {
            return "Bandit [" + getBanditName() + "] detected.";
        }
    }

    @Override
    public TYPE getType() {
        return TYPE.BANDIT_CONTACT;
    }

    public String getBanditName() {
        return (String) getFields().get(FieldNames.BANDIT_NAME);
    }

    public String getBanditUUID() {
        return (String) getFields().get(FieldNames.BANDIT_UUID);
    }

    @Override
    public boolean sameAs(Alert alert) {
        if (!(alert instanceof BanditContactAlert)) {
            return false;
        }

        BanditContactAlert a = (BanditContactAlert) alert;

        if (getFields().containsKey(FieldNames.SSID)) {
            if (!a.getFields().containsKey(FieldNames.SSID)) {
                return false;
            } else {
                if (!getFields().get(FieldNames.SSID).equals(a.getFields().get(FieldNames.SSID))) {
                    return false;
                }
            }
        }

        return a.getBanditUUID().equals(this.getBanditUUID());
    }

    public static BanditContactAlert create(DateTime firstSeen, String banditName, String banditUUID, Optional<String> ssid, long frameCount) {
        ImmutableMap.Builder<String, Object> fields = new ImmutableMap.Builder<>();

        fields.put(FieldNames.BANDIT_NAME, banditName);
        fields.put(FieldNames.BANDIT_UUID, banditUUID);

        if (ssid.isPresent() && !Strings.isNullOrEmpty(ssid.get())) {
            fields.put(FieldNames.SSID, ssid.get());
        }

        return new BanditContactAlert(firstSeen, Subsystem.DOT_11, fields.build(), frameCount);
    }

}
