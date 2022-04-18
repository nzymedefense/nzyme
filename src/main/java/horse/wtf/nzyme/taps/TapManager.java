package horse.wtf.nzyme.taps;

import horse.wtf.nzyme.NzymeLeader;
import horse.wtf.nzyme.rest.resources.taps.reports.StatusReport;

import java.util.List;

public class TapManager {

    private final NzymeLeader nzyme;

    public TapManager(NzymeLeader nzyme) {
        this.nzyme = nzyme;
    }

    public void registerTapStatus(StatusReport report) {

    }

    public List<Tap> findAllTaps() {
        return nzyme.getDatabase().withHandle(handle -> handle.createQuery("SELECT * FROM taps;")
                .mapTo(Tap.class)
                .list());
    }

}
