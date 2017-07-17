package horse.wtf.nzyme;

import horse.wtf.nzyme.channels.ChannelHopper;
import horse.wtf.nzyme.configuration.Configuration;
import horse.wtf.nzyme.dot11.Dot11MetaInformation;
import horse.wtf.nzyme.graylog.Notification;
import horse.wtf.nzyme.statistics.Statistics;

public class MockNzyme implements Nzyme {

    private boolean inLoop = false;

    @Override
    public void loop() throws NzymeInitializationException {
        inLoop = true;
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
    public int getStatsInterval() {
        return Integer.MAX_VALUE;
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
}
