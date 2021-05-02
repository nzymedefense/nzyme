import React from 'react';
import LoadingSpinner from "../../misc/LoadingSpinner";
import ProbeTableRow from "../ProbeTableRow";
import SSIDAsset from "./SSIDAsset";

class Dot11NetworkAssetsList extends React.Component {

    render() {
        const self = this;

        if (!this.props.ssids) {
            return <LoadingSpinner/>;
        } else {
            return (
                <div className="row assets-ssid">
                    <div className="col-md-12">
                        {Object.keys(this.props.ssids).map(function (key,i) {
                            return <SSIDAsset key={i} ssid={self.props.ssids[key]} />;
                        })}
                    </div>
                </div>
            )
        }
    }

}

export default Dot11NetworkAssetsList;