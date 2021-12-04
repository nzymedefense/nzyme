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

package horse.wtf.nzyme.reporting.reports;

import horse.wtf.nzyme.MockNzyme;
import horse.wtf.nzyme.dot11.MalformedFrameException;
import org.testng.annotations.Test;

public class WirelessInventoryReportTest {

    @Test
    public void testBasicReport() throws Exception, MalformedFrameException {
        MockNzyme nzyme = new MockNzyme();

        WirelessInventoryReport.Report report = new WirelessInventoryReport.Report();
        report.runReport(nzyme, null);
    }

}
