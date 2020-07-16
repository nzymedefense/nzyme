import React from 'react';
import Reflux from 'reflux';
import SSIDRow from "./SSIDRow";
import NetworksStore from "../../stores/NetworksStore";
import NetworksActions from "../../actions/NetworksActions";
import LoadingSpinner from "../misc/LoadingSpinner";

class SSIDTableRow extends Reflux.Component {

    constructor(props) {
        super(props);

        this.store = NetworksStore;

        this.state = {
            ssid: undefined
        };
    }

    componentDidMount() {
        const ssid = this.props.ssid;
        const bssid = this.props.bssid;

        this.setState({ssid: undefined});

        if (ssid !== "[not human readable]") {
            NetworksActions.findSSIDOnBSSID(bssid, ssid);
        }
    }

    _findMostActiveChannel(channels) {
        let mostActive = 0;

        for(const x in channels) {
            const channel = channels[x];

            if(channel.total_frames_recent > mostActive) {
                mostActive = channel.channel_number;
            }
        }

        console.log(mostActive);

        return mostActive;
    }

    render() {
        const self = this;

        if (this.props.ssid === "[not human readable]") {
            return (
                <tr>
                    <td colSpan="7" style={{textAlign: "center"}}>
                        Not showing details for hidden or not human readable SSIDs.
                    </td>
                </tr>
            )
        }

        if (!this.state.ssid) {
            return (
                <tr>
                    <td colSpan="7">
                        <LoadingSpinner />
                    </td>
                </tr>
            )
        }

        const mostActiveChannel = this._findMostActiveChannel(this.state.ssid.channels);

        return (
            <tr>
                <td colSpan="7">
                    <table className="table table-sm table-hover table-striped">
                        <thead>
                            <tr>
                                <th>SSID</th>
                                <th>Channel</th>
                                <th title="Frames per Second">FPS</th>
                                <th>Security</th>
                            </tr>
                        </thead>
                        <tbody>
                        {Object.keys(this.state.ssid.channels).map(function (key,i) {
                            return <SSIDRow
                                key={"ssidrow-" + self.props.bssid + "-" + self.state.ssid.name + "-" + key}
                                ssid={self.state.ssid}
                                channel={self.state.ssid.channels[key]}
                                isMostActiveChannel={mostActiveChannel === self.state.ssid.channels[key].channel_number}
                            />;
                        })}
                        </tbody>
                    </table>
                </td>
            </tr>
        )
    }

}

export default SSIDTableRow;