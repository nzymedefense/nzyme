import React from 'react';
import Reflux from 'reflux';
import LoadingSpinner from "../../misc/LoadingSpinner";
import TrackersActions from "../../../actions/TrackersActions";
import TrackersStore from "../../../stores/TrackersStore";
import TrackersTableRow from "./TrackersTableRow";

class TrackersTable extends Reflux.Component {

    constructor(props) {
        super(props);

        this.store = TrackersStore;

        this.state = {
            trackers: undefined
        }
    }

    componentDidMount() {
        TrackersActions.findAll();

        setInterval(function () {
            TrackersActions.findAll();
        }, 5000);
    }

    render() {
        if (!this.state.trackers) {
            return <LoadingSpinner />
        }

        const trackers = this.state.trackers;

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
                            <th>Last Contact</th>
                            <th>Signal Strength</th>
                        </tr>
                        </thead>
                        <tbody>
                        {Object.keys(trackers).map(function (key,i) {
                            return <TrackersTableRow key={"tracker-"+key} tracker={trackers[key]} />
                        })}
                        </tbody>
                    </table>
                </div>
            </div>
        );
    }

}

export default TrackersTable;