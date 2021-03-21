import React from 'react';

import LoadingSpinner from "../alerts/AlertsTable";

import BSSIDTable from "./BSSIDTable";
import NetworksService from "../../services/NetworksService";

class NetworksList extends React.Component {

    constructor(props) {
        super(props);

        this.state = {
            bssids: undefined,
            filter: ""
        };

        this.service = new NetworksService();
        this.service.findAll = this.service.findAll.bind(this);
        this.service.resetNetworks = this.service.resetNetworks.bind(this);

        this.filterInput = React.createRef();

        this._applyFilter = this._applyFilter.bind(this);
        this._updateFilter = this._updateFilter.bind(this);
        this._resetNetworks = this._resetNetworks.bind(this);
    }

    componentDidMount() {
        const self = this;

        this.service.findAll(self.state.filter);
        setInterval(function () {
            self.service.findAll(self.state.filter)
        }, 15000);
    }

    _applyFilter(e) {
        e.preventDefault();

        const value = this.filterInput.current.value;
        this.setState({filter: value});
        this.service.findAll(value);
    }

    _updateFilter() {
        this.setState();
    }

    _resetNetworks(e) {
        e.preventDefault();

        if (window.confirm("Reset Networks? This will remove current meta information about networks but not persisted data." +
            " This can be useful after an attacker spammed a lot of network names and you don't want to wait for old networks" +
            " to be retention cleaned.")) {
            const self = this;
            this.service.resetNetworks(function () {self.service.findAll(self.state.filter)});
        }
    }

    render() {
        if (!this.state.bssids) {
            return <LoadingSpinner />;
        } else {
            return (
                <div>
                    <div className="row">
                        <div className="col-md-6">
                            <form onSubmit={this._applyFilter}>
                                <input type="text" name="filter" ref={this.filterInput} style={{float:"left",marginRight:5}} />
                                <input type="submit" value="Filter" />
                            </form>
                        </div>

                        <div className="col-md-6 text-right">
                            <form onSubmit={this._resetNetworks}>
                                <input type="submit" value="Reset Networks" />
                            </form>
                        </div>
                    </div>

                    <div className="row mt-3">
                        <div className="col-md-12">
                            <BSSIDTable bssids={this.state.bssids}/>
                        </div>
                    </div>
                </div>
            );
        }
    }

}

export default NetworksList;



