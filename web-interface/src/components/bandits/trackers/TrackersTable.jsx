import React from 'react';
import Reflux from 'reflux';
import LoadingSpinner from "../../misc/LoadingSpinner";
import TrackersActions from "../../../actions/TrackersActions";
import TrackersStore from "../../../stores/TrackersStore";
import TrackersTableRow from "./TrackersTableRow";
import BanditsActions from "../../../actions/BanditsActions";
import BanditsStore from "../../../stores/BanditsStore";
import GroundStationDisabled from "./GroundStationDisabled";
import {notify} from "react-notify-toast";

class TrackersTable extends Reflux.Component {

    constructor(props) {
        super(props);

        this.stores = [TrackersStore, BanditsStore];

        this._onTrackingStartButtonClicked = this._onTrackingStartButtonClicked.bind(this);

        this.state = {
            trackers: undefined,
            bandits: undefined,
            groundstationEnabled: undefined
        }
    }

    componentDidMount() {
        TrackersActions.findAll();
        BanditsActions.findAll();

        setInterval(function () {
            TrackersActions.findAll();
            BanditsActions.findAll();
        }, 5000);
    }

    _onTrackingStartButtonClicked(e, trackerName) {
        e.preventDefault();
        const self = this;

        TrackersActions.issueTrackingRequest(trackerName, this.state.bandit.uuid, function() {
            notify.show("Issued tracking request for this bandit to tracker device.", "success");
            self.setState({ state: this.state });
        })
    }

    render() {
        if (!this.state.trackers || !this.state.bandits) {
            return <LoadingSpinner />
        }

        if (!this.state.groundstationEnabled) {
            return <GroundStationDisabled />;
        }

        const trackers = this.state.trackers;
        const bandits = this.state.bandits;
        const forBandit = this.props.forBandit;

        const self = this;

        if (trackers.length === 0) {
            return (
                <div className="alert alert-info">
                    No tracker devices have connected yet.
                </div>
            )
        }

        return (
            <div className="row">
                <div className="col-md-12">
                    <table className="table table-sm table-hover table-striped">
                        <thead>
                        <tr>
                            <th>Name</th>
                            <th>Status</th>
                            <th>Tracking Mode</th>
                            <th>Last Ping</th>
                            <th>Signal Strength</th>
                            <th>&nbsp;</th>
                        </tr>
                        </thead>
                        <tbody>
                        {Object.keys(trackers).map(function (key,i) {
                            return <TrackersTableRow
                                key={"tracker-"+key}
                                tracker={trackers[key]}
                                totalBandits={bandits.length}
                                forBandit={forBandit}
                                onTrackingStartButtonClicked={self._onTrackingStartButtonClicked} />
                        })}
                        </tbody>
                    </table>
                </div>
            </div>
        );
    }

}

export default TrackersTable;