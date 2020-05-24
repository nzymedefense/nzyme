import React from 'react';
import Reflux from 'reflux';

import GroundStationDisabled from "./trackers/GroundStationDisabled";
import TrackersTable from "./trackers/TrackersTable";

class BanditTracking extends Reflux.Component {

    render() {
        if (!this.props.groundstationEnabled) {
            return <GroundStationDisabled />;
        }

        return (
            <div className="row">
                <div className="col-md-12">
                    <p>Instruct idle tracking devices to start locating this bandit:</p>

                    <TrackersTable forBandit={this.props.bandit}/>
                </div>
            </div>
        )
    }

}

export default BanditTracking;