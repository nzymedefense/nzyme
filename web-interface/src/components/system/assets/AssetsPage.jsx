import React from 'react';
import AssetInventoryService from "../../../services/AssetInventoryService";
import Dot11NetworkAssetsList from "./Dot11NetworkAssetsList";
import CSVExport from "./CSVExport";
import Routes from "../../../util/Routes";

class AssetsPage extends React.Component {

    constructor(props) {
        super(props);

        this.state = {
            ssids: undefined,
            ssids_csv: undefined,
            bssids_csv: undefined,
            csv_visible: false
        }

        this.assetsService = new AssetInventoryService();
        this.assetsService.findAllDot11Assets = this.assetsService.findAllDot11Assets.bind(this);

        this._triggerCSV = this._triggerCSV.bind(this);
    }

    componentDidMount() {
        this.assetsService.findAllDot11Assets();
    }

    _triggerCSV() {
        const isVisible = this.state.csv_visible;
        this.setState({csv_visible: !isVisible});
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

                    <div className="col-md-3">
                        <div className="float-right">
                            <button className="btn btn-primary" onClick={this._triggerCSV}>
                                {this.state.csv_visible ? "Hide" : "Show as"} CSV
                            </button>
                        </div>
                    </div>
                </div>

                <div className="row" style={{display: this.state.csv_visible ? "block" : "none"}}>
                    <div className="col-md-12">
                        <CSVExport ssids={this.state.ssids_csv} bssids={this.state.bssids_csv} />
                    </div>
                </div>

                <div className="row">
                    <div className="col-md-12">
                        <h2>802.11 Networks</h2>
                    </div>
                </div>

                <div className="row">
                    <div className="col-md-12">
                        <Dot11NetworkAssetsList ssids={this.state.ssids}/>
                    </div>
                </div>
            </div>
        )
    }

}

export default AssetsPage;