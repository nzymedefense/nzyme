import React from 'react';
import Reflux from 'reflux';

import LoadingSpinner from "../overview/AlertsList";

import NetworksActions from "../../actions/NetworksActions";
import NetworksStore from "../../stores/NetworksStore";
import BSSIDTable from "./BSSIDTable";

class NetworksList extends Reflux.Component {

    constructor(props) {
        super(props);

        this.store = NetworksStore;

        this.state = {
            bssids: undefined,
            filter: ""
        }

        this.filterInput = React.createRef();

        this._applyFilter = this._applyFilter.bind(this);
        this._updateFilter = this._updateFilter.bind(this);
    }

    componentDidMount() {
        const self = this;

        NetworksActions.findAll(self.state.filter);
        setInterval(function () {
            NetworksActions.findAll(self.state.filter)
        }, 1000);
    }

    _applyFilter(e) {
        e.preventDefault();

        const value = this.filterInput.current.value;
        this.setState({filter: value});
        NetworksActions.findAll(value);
    }

    _updateFilter() {
        this.setState();
    }

    render() {
        if (!this.state.bssids) {
            return <LoadingSpinner />;
        } else {
            return (
                <div>
                    <div className="row">
                        <div className="col-md-12">
                            Filter:

                            <form onSubmit={this._applyFilter}>
                                <input type="text" name="filter" ref={this.filterInput} style={{float:"left",marginRight:5}} />
                                <input type="submit" value="Filter" />
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



