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

import javax.validation.constraints.NotNull;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class ProbeFailureAlert extends Alert {

    private static final String DESCRIPTION = "An nzyme probe/adapter failed. It might have been disconnected, stopped on the " +
            "operating system level or there could be a driver issue. Check your nzyme log file and follow the documentation.";
    private static final String DOC_LINK = "guidance-PROBE_FAILURE";
    private static final List<String> FALSE_POSITIVES = new ArrayList<String>(){{
        add("In certain low-traffic environments, a probe might appear idle if no frames have been recorded because no frames existed. " +
                "Follow the nzyme documentation to configure the `max_idle_time_seconds` variable accordingly.");
    }};

    private ProbeFailureAlert(DateTime timestamp, Map<String, Object> fields) {
        super(timestamp, Subsystem.NZYME_SYS, fields, DESCRIPTION, DOC_LINK, FALSE_POSITIVES, false, -1);
    }

    @Override
    public String getMessage() {
        return "Probe [" + getProbeName() + "] failed. (" + getErrorDescription() + ")";
    }

    public String getProbeName() {
        return (String) getFields().get(FieldNames.PROBE_NAME);
    }

    public String getErrorDescription() {
        return (String) getFields().get(FieldNames.ERROR_DESCRIPTION);
    }

    @Override
    public TYPE getType() {
        return TYPE.PROBE_FAILURE;
    }

    @Override
    public boolean sameAs(Alert alert) {
        if (!(alert instanceof ProbeFailureAlert)) {
            return false;
        }

        ProbeFailureAlert a = (ProbeFailureAlert) alert;
        return a.getProbeName().equals(this.getProbeName());
    }

    public static ProbeFailureAlert create(DateTime firstSeen, @NotNull String probeName, @NotNull String errorDescription) {
        if (Strings.isNullOrEmpty(probeName)) {
            throw new IllegalArgumentException("Probe name cannot be empty.");
        }

        if (Strings.isNullOrEmpty(errorDescription)) {
            throw new IllegalArgumentException("Error description cannot be empty.");
        }

        ImmutableMap.Builder<String, Object> fields = new ImmutableMap.Builder<>();
        fields.put(FieldNames.PROBE_NAME, probeName);
        fields.put(FieldNames.ERROR_DESCRIPTION, errorDescription);

        return new ProbeFailureAlert(firstSeen, fields.build());
    }

}
