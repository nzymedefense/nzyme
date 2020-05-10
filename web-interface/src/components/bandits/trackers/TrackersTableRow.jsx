import React from 'react';
import Reflux from 'reflux';
import moment from "moment";
import {round} from "lodash";
import TrackerStatus from "./TrackerStatus";
import TrackingMode from "./TrackingMode";

class TrackersTableRow extends Reflux.Component {

    render() {
        const tracker = this.props.tracker;

        return (
            <tr>
                <td>{tracker.name}</td>
                <td><TrackerStatus status={tracker.state} banditCount={tracker.bandit_count} totalBandits={this.props.totalBandits}/></td>
                <td><TrackingMode mode={tracker.tracking_mode} /></td>
                <td title={moment(tracker.last_seen).format()}>
                    {moment(tracker.last_seen).fromNow()}
                </td>
                <td>{round(tracker.rssi/255*100)}%</td>
            </tr>
        );
    }

}

export default TrackersTableRow;