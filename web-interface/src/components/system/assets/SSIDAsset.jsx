import React from 'react';
import {join} from "lodash/array";
import BSSIDAssetRow from "./BSSIDAssetRow";

class SSIDAsset extends React.Component {

    render() {
        const self = this;

        return (
            <div className="assets-ssid">
                <hr />

                <h3>SSID: {this.props.ssid.ssid}</h3>

                <dl>
                    <dt>Security:</dt>
                    <dd>{join(this.props.ssid.security, ", ")}</dd>
                    <dt>Channels:</dt>
                    <dd>{join(this.props.ssid.channels, ", ")}</dd>
                </dl>

                <h4>Access Points</h4>

                <table className="table table-sm table-hover table-striped">
                    <thead>
                    <tr>
                        <th>BSSID</th>
                        <th>Fingerprints</th>
                    </tr>
                    </thead>
                    <tbody>
                    {Object.keys(this.props.ssid.bssids).map(function (key,i) {
                        return <BSSIDAssetRow key={i} bssid={self.props.ssid.bssids[key]} />;
                    })}
                    </tbody>
                </table>
            </div>
        )
    }

}

export default SSIDAsset;