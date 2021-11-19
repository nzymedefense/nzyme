import React from 'react';
import LoadingSpinner from "../misc/LoadingSpinner";
import BanditsService from "../../services/BanditsService";
import Routes from "../../util/Routes";
import moment from "moment";
import Timespan from "../misc/Timespan";
import RSSI from "../misc/RSSI";
import numeral from "numeral";
import AdvertisedSSIDTable from "./AdvertisedSSIDTable";
import AdvertisedBSSIDTable from "./AdvertisedBSSIDTable";
import SimpleBarChart from "../charts/SimpleBarChart";

class BanditContactDetailsPage extends React.Component {

    constructor(props) {
        super(props);

        this.banditUUID = decodeURIComponent(props.match.params.banditUUID);
        this.contactUUID = decodeURIComponent(props.match.params.contactUUID);

        this.state = {
            contact: undefined,
            detailed_ssids: [],
            detailed_bssids: []
        }

        this.banditsService = new BanditsService();
        this.banditsService.findContactOfBandit = this.banditsService.findContactOfBandit.bind(this);

        this._loadData = this._loadData.bind(this);
        this._formatFrameCountHistogram = this._formatFrameCountHistogram.bind(this);
        this._onNewSSIDSelection = this._onNewSSIDSelection.bind(this);
        this._onNewBSSIDSelection = this._onNewBSSIDSelection.bind(this);
    }

    componentDidMount() {
        const self = this;
        self._loadData()

        setInterval(function () {
            self._loadData()
        }, 5000);
    }

    _loadData() {
        this.banditsService.findContactOfBandit(this.banditUUID, this.contactUUID, this.state.detailed_ssids.join(), this.state.detailed_bssids.join());
    }

    _formatFrameCountHistogram(counts) {
        const result = [];

        Object.keys(counts).forEach(function (key) {
            let x = [];
            let y = [];

            Object.keys(counts[key]).sort().forEach(function (countKey) {
                x.push(new Date(countKey));
                y.push(counts[key][countKey]);
            });

            result.push({
                x: x,
                y: y,
                type: "line",
                name: key,
                line: {width: 1, shape: "linear"},
            })
        });

        return result;
    }

    _onNewSSIDSelection(selection) {
        const self = this;
        this.setState({detailed_ssids: selection}, function () {
            self._loadData();
        });
    }

    _onNewBSSIDSelection(selection) {
        const self = this;
        this.setState({detailed_bssids: selection}, function () {
            self._loadData();
        });
    }

    render() {
        if (!this.state.contact) {
            return <LoadingSpinner />
        }

        const contact = this.state.contact;

        return (
            <div>
                <div className="row">
                    <div className="col-md-12">
                        <nav aria-label="breadcrumb">
                            <ol className="breadcrumb">
                                <li className="breadcrumb-item"><a href={Routes.BANDITS.INDEX}>Bandits</a></li>
                                <li className="breadcrumb-item active" aria-current="page">
                                    <a href={Routes.BANDITS.SHOW(contact.bandit_uuid)}>{contact.bandit_name}</a>
                                </li>
                                <li className="breadcrumb-item active" aria-current="page">Contact Details</li>
                            </ol>
                        </nav>
                    </div>
                </div>

                <div className="row">
                    <div className="col-md-12">
                        <h1>
                            Bandit Contact <em>{contact.uuid.substr(0, 8)}</em>&nbsp;
                            <small>{contact.is_active ? <span className="badge badge-success pull-right contact-status">active</span> : <span className="badge badge-primary pull-right contact-status">not active</span>}</small>
                        </h1>
                    </div>
                </div>

                <div className="row">
                    <div className="col-md-7">
                        <dl>
                            <dt>Bandit:</dt>
                            <dd>{contact.bandit_name}</dd>
                        </dl>
                    </div>
                    <div className="col-md-4">
                        <dl>
                            <dt>Contact ID:</dt>
                            <dd>{contact.uuid}</dd>
                        </dl>
                    </div>
                    <div className="col-md-1">
                        <a href={Routes.BANDITS.SHOW(contact.bandit_uuid)} className="btn btn-dark">Back</a>
                    </div>
                </div>

                <div className="row">
                    <div className="col-md-12">
                        <hr />
                    </div>
                </div>

                <div className="row">
                    <div className="col-md-2">
                        <dl>
                            <dt>First Seen:</dt>
                            <dd title={moment(contact.first_seen).format()}>
                                {moment(contact.first_seen).format("M/D/YY hh:mm a")}
                            </dd>
                        </dl>
                    </div>

                    <div className="col-md-2">
                        <dl>
                            <dt>Last Seen:</dt>
                            <dd title={moment(contact.last_seen).format()}>
                                {moment(contact.last_seen).format("M/D/YY hh:mm a")}
                            </dd>
                        </dl>
                    </div>

                    <div className="col-md-2">
                        <dl>
                            <dt>Duration:</dt>
                            <dd><Timespan from={contact.first_seen} to={contact.last_seen} /></dd>
                        </dl>
                    </div>

                    <div className="col-md-2">
                        <dl>
                            <dt>Frames:</dt>
                            <dd>{numeral(contact.frame_count).format('0,0')}</dd>
                        </dl>
                    </div>

                    <div className="col-md-2">
                        <dl>
                            <dt>Signal:</dt>
                            <dd>{contact.is_active ? <RSSI rssi={contact.last_signal} /> : "n/a"}</dd>
                        </dl>
                    </div>
                </div>

                <div className="row mt-3">
                    <div className="col-md-6">
                        <h2>Advertised SSIDs</h2>
                        <AdvertisedSSIDTable ssids={contact.ssids} onNewSelection={this._onNewSSIDSelection} />
                    </div>
                    <div className="col-md-6">
                        <h2>Advertised BSSIDs</h2>
                        <AdvertisedBSSIDTable bssids={contact.bssids} onNewSelection={this._onNewBSSIDSelection} />
                    </div>
                </div>

                <div className="row">
                    <div className="col-md-6">
                        <SimpleBarChart
                            title="Frame Count (by SSID, most recent 24h max)"
                            showLegend={true}
                            width={540}
                            height={150}
                            customMarginTop={30}
                            finalData={this._formatFrameCountHistogram(this.state.contact.ssid_frame_count_histograms)}/>
                    </div>

                    <div className="col-md-6">
                        <SimpleBarChart
                            title="Frame Count (by BSSID, most recent 24h max)"
                            showLegend={true}
                            width={545}
                            height={150}
                            customMarginTop={30}
                            finalData={this._formatFrameCountHistogram(this.state.contact.bssid_frame_count_histograms)}/>
                    </div>
                </div>

            </div>
        )
    }

}

export default BanditContactDetailsPage;