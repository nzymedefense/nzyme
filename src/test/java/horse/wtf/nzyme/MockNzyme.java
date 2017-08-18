/*
 *  This file is part of Nzyme.
 *
 *  Nzyme is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  Nzyme is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with Nzyme.  If not, see <http://www.gnu.org/licenses/>.
 */

package horse.wtf.nzyme;

import horse.wtf.nzyme.channels.ChannelHopper;
import horse.wtf.nzyme.configuration.Configuration;
import horse.wtf.nzyme.dot11.Dot11MetaInformation;
import horse.wtf.nzyme.notifications.Notification;
import horse.wtf.nzyme.statistics.Statistics;

public class MockNzyme implements Nzyme {

    private boolean inLoop = false;

    @Override
    public Runnable loop() throws NzymeInitializationException {
        inLoop = true;

        return () -> { /* noop */ };
    }

    @Override
    public boolean isInLoop() {
        return inLoop;
    }

    @Override
    public void notify(Notification notification, Dot11MetaInformation meta) {
        // noop
    }

    @Override
    public Statistics getStatistics() {
        return null;
    }

    @Override
    public ChannelHopper getChannelHopper() {
        return null;
    }

    @Override
    public Configuration getConfiguration() {
        return null;
    }

    @Override
    public String getNetworkInterface() {
        return "FOO";
    }

}
