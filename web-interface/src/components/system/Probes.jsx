import React from 'react';
import Reflux from 'reflux';
import ProbesStore from "../../stores/ProbesStore";
import ProbesActions from "../../actions/ProbesActions";

import ProbesList from "./ProbesList";

class Probes extends Reflux.Component {

    constructor(props) {
        super(props);

        this.store = ProbesStore;

        this.state = {
            probes: undefined
        };
    }

    componentDidMount() {
        ProbesActions.findAll();
        setInterval(function () {
            ProbesActions.findAll();
        }, 1000);
    }

    render() {
        return (
            <div>
                <div className="row">
                    <div className="col-md-12">
                        <h3>Probes</h3>

                        <ProbesList probes={this.state.probes} />
                    </div>
                </div>
            </div>
        )
    }

}

export default Probes;