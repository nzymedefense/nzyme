import React from 'react';
import Reflux from 'reflux';
import LoadingSpinner from "../../misc/LoadingSpinner";
import TrackersActions from "../../../actions/TrackersActions";
import TrackersStore from "../../../stores/TrackersStore";
import TrackersTableRow from "./TrackersTableRow";
import BanditsActions from "../../../actions/BanditsActions";
import BanditsStore from "../../../stores/BanditsStore";

class TrackersTable extends Reflux.Component {

    constructor(props) {
        super(props);

        this.stores = [TrackersStore, BanditsStore];

        this.state = {
            trackers: undefined,
            bandits: undefined
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

    render() {
        if (!this.state.trackers || !this.state.bandits) {
            return <LoadingSpinner />
        }

        const trackers = this.state.trackers;
        const bandits = this.state.bandits;

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
                            <th>Bandit Sync</th>
                            <th>Last Contact</th>
                            <th>Signal Strength</th>
                        </tr>
                        </thead>
                        <tbody>
                        {Object.keys(trackers).map(function (key,i) {
                            return <TrackersTableRow key={"tracker-"+key} tracker={trackers[key]} totalBandits={bandits.length} />
                        })}
                        </tbody>
                    </table>
                </div>
            </div>
        );
    }

}

export default TrackersTable;