import React, { useCallback }  from 'react';

import Routes from '../../util/ApiRoutes'
import { anyTrackerTrackingBandit } from './BanditTools';

function CreateIdentifierButton(props) {

    const createIdentifier = useCallback((e) => {
        if (props.bandit.read_only) {
            alert('Cannot create identifier for built-in bandit.');
            e.preventDefault();
        }

        if (anyTrackerTrackingBandit(props.bandit, props.trackers)) {
            alert('Cannot create identifier for bandit that is currently tracked by trackers. Please stop tracking first.');
            e.preventDefault();
        }
    }, [props.bandit, props.trackers]);

    return (
        <a href={Routes.BANDITS.NEW_IDENTIFIER(props.bandit.uuid)} className="btn btn-success float-right" onClick={createIdentifier}>
            Create Identifier
        </a>
    );

}

export default CreateIdentifierButton;