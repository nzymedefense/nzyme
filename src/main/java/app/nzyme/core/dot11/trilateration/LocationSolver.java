package app.nzyme.core.dot11.trilateration;

import app.nzyme.core.dot11.db.TapBasedSignalStrengthResult;
import com.google.common.collect.Lists;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.List;

public class LocationSolver {

    private static final Logger LOG = LogManager.getLogger(LocationSolver.class);

    public static void solve(List<TapBasedSignalStrengthResult> taps) {
        List<Double[]> positions = Lists.newArrayList();
        List<Double> distances = Lists.newArrayList();

        for (TapBasedSignalStrengthResult s : taps) {
            double distance = Math.pow(10, (double)(-40 - (s.signalStrength())) / (10 * 1));
            LOG.info("Distance from [{}] ({} dBm): {} meters", s.signalStrength(), s.tapName(), distance);
        }

        // https://github.com/lemmingapex/Trilateration

    }

}
