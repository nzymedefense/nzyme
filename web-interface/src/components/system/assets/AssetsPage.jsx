import React from 'react';
import AssetInventoryService from "../../../services/AssetInventoryService";
import Dot11NetworksTable from "./Dot11NetworksTable";

class AssetsPage extends React.Component {

    constructor(props) {
        super(props);

        this.state = {
            ssids: undefined
        }

        this.assetsService = new AssetInventoryService();
        this.assetsService.findAllDot11Assets = this.assetsService.findAllDot11Assets.bind(this);
    }

    componentDidMount() {
        this.assetsService.findAllDot11Assets();
    }

    render() {
        return (
            <div>
                <div className="row">
                    <div className="col-md-12">
                        <h1>Assets</h1>
                    </div>
                </div>

                <div className="row">
                    <div className="col-md-9">
                        <p>
                            The asset inventory lists all expected assets as configured in your configuration file. This
                            can be useful to confirm the configuration file is complete and it can also satisfy some
                            compliance rules that require you to keep an up to date inventory of wireless access points.
                        </p>
                    </div>
                </div>

                <hr />

                <div className="row">
                    <div className="col-md-12">
                        <h2>802.11 Networks</h2>
                    </div>
                </div>

                <div className="row">
                    <div className="col-md-12">
                        <Dot11NetworksTable ssids={this.state.ssids}/>
                    </div>
                </div>
            </div>
        )
    }

}

export default AssetsPage;