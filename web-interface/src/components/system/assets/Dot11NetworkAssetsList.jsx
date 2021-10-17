import React from 'react';
import LoadingSpinner from "../../misc/LoadingSpinner";
import SSIDAsset from "./SSIDAsset";

class Dot11NetworkAssetsList extends React.Component {

    render() {
        const self = this;

        if (!this.props.ssids) {
            return <LoadingSpinner/>;
        } else {
            if (this.props.ssids.length === 0) {
                return (
                    <div className="row assets-ssid">
                        <div className="col-md-12">
                            <div className="alert alert-warning">
                                No 802.11 networks configured for monitoring in your nzyme configuration file. Learn
                                how to configure networks in
                                the <a href="https://go.nzyme.org/network-monitoring" target="_blank" rel="noopener noreferrer">documentation</a>.
                            </div>
                        </div>
                    </div>
                )
            }

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