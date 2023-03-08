package app.nzyme.core.crypto.tls;

import app.nzyme.core.distributed.Node;
import com.google.common.collect.Lists;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.List;

public class TLSWildcardNodeMatcher {

    private static final Logger LOG = LogManager.getLogger(TLSWildcardNodeMatcher.class);

    public List<Node> match(String regex, List<Node> nodes) {
        List<Node> matches = Lists.newArrayList();

        for (Node node : nodes) {
            try {
                if (node.name().matches(regex)) {
                    matches.add(node);
                }
            } catch(Exception e) {
                LOG.error("Could not apply TLS wildcard certificate node matcher. Skipping.", e);
            }
        }

        return matches;
    }

}
