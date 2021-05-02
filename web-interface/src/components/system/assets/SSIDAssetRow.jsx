import React from 'react';
import {join} from "lodash/array";
import BSSIDAssetRow from "./BSSIDAssetRow";

class SSIDAssetRow extends React.Component {

    render() {
        const self = this;

        return (
            <React.Fragment>
                <tr>
                    <td>{this.props.ssid.ssid}</td>
                    <td>{join(this.props.ssid.security, ", ")}</td>
                    <td>{join(this.props.ssid.channels, ", ")}</td>
                </tr>
                <tr>
                    <td colspan="3">
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
                    </td>
                </tr>
            </React.Fragment>
        )
    }

}

export default SSIDAssetRow;