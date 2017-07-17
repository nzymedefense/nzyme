package horse.wtf.nzyme;

import horse.wtf.nzyme.channels.ChannelHopper;
import horse.wtf.nzyme.configuration.Configuration;
import horse.wtf.nzyme.dot11.Dot11MetaInformation;
import horse.wtf.nzyme.graylog.Notification;
import horse.wtf.nzyme.statistics.Statistics;

public interface Nzyme {

    void loop() throws NzymeInitializationException;
    boolean isInLoop();

    void notify(Notification notification, Dot11MetaInformation meta);

    int getStatsInterval();

    Statistics getStatistics();
    ChannelHopper getChannelHopper();
    Configuration getConfiguration();
}
