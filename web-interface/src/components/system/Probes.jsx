import React from 'react';
import Reflux from 'reflux';
import ProbesStore from "../../stores/ProbesStore";
import ProbesActions from "../../actions/ProbesActions";

import ProbesTable from "./ProbesTable";
import TrapsTable from "./TrapsTable";

class Probes extends Reflux.Component {

    constructor(props) {
        super(props);

        this.store = ProbesStore;

        this.state = {
            probes: undefined,
            traps: undefined
        };

        this._loadData = this._loadData.bind(this);
    }

    componentDidMount() {
        const self = this;

        setInterval(function () {
            self._loadData();
        }, 1000);
    }

    _loadData() {
        ProbesActions.findAll();
        ProbesActions.findAllTraps();
    }

    render() {
        return (
            <div>
                <div className="row">
                    <div className="col-md-12">
                        <h3>Probes</h3>

                        <ProbesTable probes={this.state.probes} />
                    </div>
                </div>

                <div className="row">
                    <div className="col-md-12">
                        <h3>Traps</h3>

                        <TrapsTable traps={this.state.traps} />
                    </div>
                </div>
            </div>
        )
    }

}

export default Probes;