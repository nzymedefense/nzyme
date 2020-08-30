import React from 'react';
import LoadingSpinner from "../../misc/LoadingSpinner";
import TrackersTableRow from "./TrackersTableRow";
import GroundStationDisabled from "./GroundStationDisabled";
import {notify} from "react-notify-toast";
import BanditsService from "../../../services/BanditsService";
import TrackersService from "../../../services/TrackersService";

class TrackersTable extends React.Component {

    constructor(props) {
        super(props);

        this._onTrackingStartButtonClicked = this._onTrackingStartButtonClicked.bind(this);
        this._onTrackingCancelButtonClicked = this._onTrackingCancelButtonClicked.bind(this);

        this._loadAll = this._loadAll.bind(this);

        this.state = {
            trackers: undefined,
            bandits: undefined,
            groundstationEnabled: undefined
        }

        this.banditsService = new BanditsService();
        this.banditsService.findAll = this.banditsService.findAll.bind(this);

        this.trackersService = new TrackersService();
        this.trackersService.findAll = this.trackersService.findAll.bind(this);
        this.trackersService.issueStartTrackingRequest = this.trackersService.issueStartTrackingRequest.bind(this);
        this.trackersService.issueCancelTrackingRequest = this.trackersService.issueCancelTrackingRequest.bind(this);
    }

    componentDidMount() {
        this._loadAll()

        const self = this;
        setInterval(function () {
            self._loadAll();
        }, 5000);
    }

    _loadAll() {
        this.trackersService.findAll();
        this.banditsService.findAll();
    }

    _onTrackingStartButtonClicked(e, trackerName) {
        e.preventDefault();
        const self = this;

        this.trackersService.issueStartTrackingRequest(trackerName, this.props.forBandit.uuid, function() {
            notify.show("Issued start tracking request for this bandit to tracker device.", "success");
            self._loadAll()
        })
    }

    _onTrackingCancelButtonClicked(e, trackerName) {
        e.preventDefault();
        const self = this;

        this.trackersService.issueCancelTrackingRequest(trackerName, this.props.forBandit.uuid, function() {
            notify.show("Issued cancel tracking request for this bandit to tracker device.", "success");
            self._loadAll()
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
                                forBandit={forBandit}
                                onTrackingStartButtonClicked={self._onTrackingStartButtonClicked}
                                onTrackingCancelButtonClicked={self._onTrackingCancelButtonClicked}
                            />
                        })}
                        </tbody>
                    </table>
                </div>
            </div>
        );
    }

}

export default TrackersTable;