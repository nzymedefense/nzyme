package app.nzyme.core.rest;

import app.nzyme.core.NzymeNode;
import app.nzyme.core.rest.authentication.AuthenticatedUser;
import com.google.common.base.Splitter;
import com.google.common.base.Strings;
import com.google.common.collect.Lists;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.List;
import java.util.UUID;

public class TapDataHandlingResource extends UserAuthenticatedResource {

    private static final Logger LOG = LogManager.getLogger(TapDataHandlingResource.class);

    protected final List<UUID> parseAndValidateTapIds(AuthenticatedUser requestingUser,
                                                      NzymeNode nzyme,
                                                      String queryParamDataCsv) {
        List<UUID> userAccessibleTaps = nzyme.getTapManager().allTapUUIDsAccessibleByUser(requestingUser);

        if (Strings.isNullOrEmpty(queryParamDataCsv)) {
            return Lists.newArrayList();
        }

        if (queryParamDataCsv.equals("*")) {
            return userAccessibleTaps;
        }

        List<UUID> uuids = Lists.newArrayList();
        for (String id : Splitter.on(",").splitToList(queryParamDataCsv)) {
            UUID tapUuid = UUID.fromString(id);

            if (userAccessibleTaps.contains(tapUuid)) {
                uuids.add(tapUuid);
            } else {
                LOG.warn("User [{}] requested data from tap <{}> but they are not allowed to access it. Skipping.",
                        requestingUser.getEmail(), tapUuid);
            }
        }

        return uuids;
    }

}
