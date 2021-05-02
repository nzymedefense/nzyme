import React from 'react';
import LoadingSpinner from "../../misc/LoadingSpinner";
import ProbeTableRow from "../ProbeTableRow";
import SSIDAssetRow from "./SSIDAssetRow";

class Dot11NetworksTable extends React.Component {

    render() {
        const self = this;

        if (!this.props.ssids) {
            return <LoadingSpinner/>;
        } else {
            return (
                <table className="table table-sm table-hover table-striped">
                    <thead>
                    <tr>
                        <th>SSID</th>
                        <th>Security</th>
                        <th>Channels</th>
                    </tr>
                    </thead>
                    <tbody>
                    {Object.keys(this.props.ssids).map(function (key,i) {
                        return <SSIDAssetRow key={i} ssid={self.props.ssids[key]} />;
                    })}
                    </tbody>
                </table>
            )
        }
    }

}

export default Dot11NetworksTable;