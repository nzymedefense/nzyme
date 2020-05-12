import React from 'react';
import Reflux from 'reflux';
import TrackersStore from "../../../stores/TrackersStore";
import TrackersActions from "../../../actions/TrackersActions";
import LoadingSpinner from "../../misc/LoadingSpinner";
import Routes from "../../../util/Routes";
import moment from "moment";
import TrackerTimeDrift from "./TrackerTimeDrift";
import TrackingModeCard from "./TrackingModeCard";
import TrackerStatusCard from "./TrackerStatusCard";
import BanditsActions from "../../../actions/BanditsActions";
import BanditsStore from "../../../stores/BanditsStore";
import TrackerBanditCount from "./TrackerBanditCount";

class TrackerDetailPage extends Reflux.Component {

    constructor(props) {
        super(props);

        this.trackerName = decodeURIComponent(props.match.params.name);

        this.stores = [TrackersStore, BanditsStore];

        this.state = {
            tracker: undefined,
            bandits: undefined
        };
    }

    componentDidMount() {
        const self = this;
        self._load();

        setInterval(function () {
            self._load();
        }, 15000);
    }

    _load() {
        TrackersActions.findOne(this.trackerName);
        BanditsActions.findAll();
    }

    render() {
        if (!this.state.tracker || !this.state.bandits) {
            return <LoadingSpinner />
        }

        const tracker = this.state.tracker;
        const bandits = this.state.bandits;

        return (
            <div>
                <div className="row">
                    <div className="col-md-12">
                        <h1>Tracker <em>{tracker.name}</em></h1>
                    </div>
                </div>

                <div className="row">
                    <div className="col-md-3">
                        <dl>
                            <dt>Last Ping:</dt>
                            <dd>
                                <span title={moment(tracker.last_seen).format()}>{moment(tracker.last_seen).fromNow()}</span>
                            </dd>
                        </dl>
                    </div>

                    <div className="col-md-3">
                        <dl>
                            <dt>Time Drift:</dt>
                            <dd><TrackerTimeDrift drift={tracker.drift} status={tracker.state} /></dd>
                        </dl>
                    </div>

                    <div className="col-md-3">
                        <dl>
                            <dt>Version:</dt>
                            <dd>{tracker.version}</dd>
                        </dl>
                    </div>

                    <div className="col-md-2">
                        <dl>
                            <dt>Bandits:</dt>
                            <dd><TrackerBanditCount trackerBanditCount={tracker.bandit_count} totalBanditCount={bandits.length} /></dd>
                        </dl>
                    </div>

                    <div className="col-md-1 text-right">
                        <a href={Routes.BANDITS.INDEX} className="btn btn-dark">Back</a>&nbsp;
                    </div>
                </div>

                <div className="row">
                    <div className="col-md-12">
                        <hr />
                    </div>
                </div>

                <div className="row">
                    <div className="col-md-6">
                        <TrackerStatusCard
                            status={tracker.state}
                            banditCount={tracker.bandit_count}
                            totalBandits={this.props.totalBandits} />
                    </div>

                    <div className="col-md-6">
                        <TrackingModeCard mode={tracker.tracking_mode} status={tracker.state} />
                    </div>
                </div>

                <div className="row">
                    <div className="col-md-12">
                        <hr />
                    </div>
                </div>

                <div className="row">
                    <div className="col-md-12">
                        <h2>Commands</h2>
                    </div>
                </div>

                <div className="row">
                    <div className="col-md-12">
                        tbd: outstanding commands. send contact request.
                    </div>
                </div>

                <div className="row">
                    <div className="col-md-12">
                        <hr />
                    </div>
                </div>

                <div className="row">
                    <div className="col-md-12">
                        <h2>Contacts</h2>
                    </div>
                </div>

                <div className="row">
                    <div className="col-md-12">
                        tbd: show if curretly in contact, at which signal level and contact tracks.
                    </div>
                </div>


            </div>
        )
    }

}

export default TrackerDetailPage;