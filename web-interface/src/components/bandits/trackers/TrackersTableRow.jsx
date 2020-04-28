import React from 'react';
import Reflux from 'reflux';
import moment from "moment";
import {round} from "lodash";
import TrackerStatus from "./TrackerStatus";

class TrackersTableRow extends Reflux.Component {

    constructor(props) {
        super(props);
    }

    render() {
        const tracker = this.props.tracker;

        return (
            <tr>
                <td>{tracker.name}</td>
                <td><TrackerStatus status={tracker.state}/></td>
                <td>{tracker.bandit_count} / {this.props.totalBandits}</td>
                <td title={moment(tracker.last_seen).format()}>
                    {moment(tracker.last_seen).fromNow()}
                </td>
                <td>{round(tracker.rssi/255*100)}%</td>
            </tr>
        );
    }

}

export default TrackersTableRow;