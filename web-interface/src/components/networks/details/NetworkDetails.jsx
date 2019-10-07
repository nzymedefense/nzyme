import React from 'react';
import Reflux from 'reflux';
import LoadingSpinner from "../../alerts/AlertsList";
import NetworksStore from "../../../stores/NetworksStore";
import NetworksActions from "../../../actions/NetworksActions";
import ChannelDetails from "./ChannelDetails";
import SimpleLineChart from "../../charts/SimpleLineChart";
import BeaconRate from "./BeaconRate";
import HelpBubble from "../../misc/HelpBubble";
import ChannelSwitcher from "./ChannelSwitcher";

class NetworkDetails extends Reflux.Component {

    constructor(props) {
        super(props);

        this.store = NetworksStore;

        this.state = {
            channelNumber: props.channelNumber,
            historyHours: 4
        };

        this._loadChannel = this._loadChannel.bind(this);
        this._changeChannel = this._changeChannel.bind(this);
        this._changeRange = this._changeRange.bind(this);
    }

    componentDidMount() {
        const load = this._loadChannel;

        load();
        setInterval(function () { load(); }, 15000);
    }

    _loadChannel() {
        NetworksActions.findSSIDOnBSSID(this.props.bssid, this.props.ssid, true, this.state.historyHours*60*60);
    }

    _formatBeaconRateHistory(data) {
        const result = [];

        const avgBeaconRate = {
            x: [],
            y: [],
            type: "bar",
            name: "Beacon Rate",
            line: {width: 1, shape: "linear", color: "#2983fe"}
        };

        data.forEach(function(point) {
            const date = new Date(point["created_at"]);
            avgBeaconRate["x"].push(date);
            avgBeaconRate["y"].push(point["rate"]);
        });

        result.push(avgBeaconRate);

        return result;
    }

    _buildBeaconRateHistoryShapes(data, threshold) {
        if (!threshold) {
            return [];
        }

        return [
            {
                type: "line",
                visible: true,
                x0: new Date(data[0].created_at),
                x1: new Date(data[data.length-1].created_at),
                y0: threshold,
                y1: threshold,
                line: {
                    color: "#8a0000",
                    dash: "dash",
                    width: 1,
                }
            }
        ];
    }

    _changeChannel(channelNumber) {
        this.setState({channelNumber: channelNumber});
    }

    _changeRange(range) {
        this.setState({historyHours: range}, () => this._loadChannel());
    }

    render() {
        const ssid = this.state.ssid;

        if (!ssid) {
            return <LoadingSpinner />;
        } else {
            return (
                <div>
                    <div className="row">
                        <div className="col-md">
                            <dl>
                                <dt>BSSID</dt>
                                <dd>{ssid.bssid}</dd>
                            </dl>
                        </div>

                        <div className="col-md-3">
                            <dl>
                                <dt>SSID</dt>
                                <dd>{ssid.name}</dd>
                            </dl>
                        </div>

                        <div className="col-md-3">
                            <dl>
                                <dt>Current Beacon Rate</dt>
                                <dd><BeaconRate rate={ssid.beacon_rate} /></dd>
                            </dl>
                        </div>
                    </div>

                    <div className="row">
                        <div className="col-md-12">
                            <hr />

                            <h3>Beacon Rate</h3>

                            <SimpleLineChart
                                title="Beacon Rate"
                                width={1100}
                                height={200}
                                customMarginLeft={60}
                                customMarginRight={60}
                                finalData={this._formatBeaconRateHistory(ssid.beacon_rate_history)}
                                shapes={this._buildBeaconRateHistoryShapes(ssid.beacon_rate_history, ssid.beacon_rate_threshold)}
                            />

                            Beacon Rate Alert Threshold: { ssid.beacon_rate_threshold ? ssid.beacon_rate_threshold : "not configured for this network"}
                        </div>
                    </div>

                    <div className="row">
                        <div className="col-md-12">
                            <hr />

                            <h3>
                                Network-wide Fingerprints <small><HelpBubble link="https://go.nzyme.org/fingerprinting" /></small>
                            </h3>

                            <ul>
                                {Object.keys(ssid.fingerprints).map(function (key,i) {
                                    return <li key={"fp-" + ssid.fingerprints[key]}>{ssid.fingerprints[key]}</li>
                                })}
                            </ul>
                        </div>
                    </div>

                    <div className="row">
                        <div className="col-md-12">
                            <hr />

                            <div className="row">
                                <div className="col-md-9">
                                    <h2>Channel {this.state.channelNumber}</h2>
                                </div>
                                <div className="col-md-3">
                                    <ChannelSwitcher
                                        channels={ssid.channels}
                                        currentChannel={this.state.channelNumber}
                                        changeChannel={this._changeChannel}
                                    />
                                </div>
                            </div>

                            <div className="row">
                                <div className="col-md-12">
                                    <ChannelDetails
                                        channel={ssid.channels[this.state.channelNumber]}
                                        ssid={ssid}
                                        historyHours={this.state.historyHours}
                                        _changeRange={this._changeRange}
                                    />
                                </div>
                            </div>
                        </div>
                    </div>
                </div>
            );
        }
    }

}

export default NetworkDetails;



