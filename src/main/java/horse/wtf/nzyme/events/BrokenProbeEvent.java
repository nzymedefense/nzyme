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

package horse.wtf.nzyme.events;

public class BrokenProbeEvent implements Event {

    private final String probeName;
    private final String errorDescription;

    public BrokenProbeEvent(String probeName, String errorDescription) {
        this.probeName = probeName;
        this.errorDescription = errorDescription;
    }

    @Override
    public TYPE type() {
        return TYPE.BROKEN_PROBE;
    }

    @Override
    public String name() {
        return "Broken Probe [" + probeName + "]";
    }

    @Override
    public String description() {
        return "Probe [" + probeName + "] is not working as expected. Check your nzyme log file. (" + errorDescription + ")";
    }
}
