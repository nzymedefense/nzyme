import React from 'react';
import moment from "moment";
import {round} from "lodash";
import TrackerStatus from "./TrackerStatus";
import TrackingMode from "./TrackingMode";
import Routes from "../../../util/Routes";
import TrackBanditButton from "./TrackBanditButton";

class TrackersTableRow extends React.Component {

    render() {
        const tracker = this.props.tracker;

        return (
            <tr>
                <td><a href={Routes.BANDITS.SHOW_TRACKER(tracker.name)}>{tracker.name}</a></td>
                <td><TrackerStatus status={tracker.state} /></td>
                <td>
                    <TrackingMode
                        mode={tracker.tracking_mode}
                        status={tracker.state}
                        bandit={this.props.forBandit}
                        pendingRequests={tracker.has_pending_tracking_requests} />
                </td>
                <td title={moment(tracker.last_seen).format()}>
                    {moment(tracker.last_seen).fromNow()}
                </td>
                <td>{round(tracker.rssi/255*100)}%</td>
                <td>
                    <TrackBanditButton
                        bandit={this.props.forBandit}
                        tracker={tracker}
                        onStartTrackingClick={(e) => { this.props.onTrackingStartButtonClicked(e, tracker.name) }}
                        onCancelTrackingClick={(e) => { this.props.onTrackingCancelButtonClicked(e, tracker.name) }}
                    />
                </td>
            </tr>
        );
    }

}

export default TrackersTableRow;