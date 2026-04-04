package app.nzyme.core.monitors;

import app.nzyme.core.NzymeNode;
import app.nzyme.core.periodicals.Periodical;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class MonitorsThread extends Periodical {

    private static final Logger LOG = LogManager.getLogger(MonitorsThread.class);

    private final NzymeNode nzyme;

    public MonitorsThread(NzymeNode nzyme) {
        this.nzyme = nzyme;
    }

    @Override
    protected void execute() {

    }

    @Override
    public String getName() {
        return "MonitorsThread";
    }
}
