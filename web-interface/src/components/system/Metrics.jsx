import React from 'react';
import Reflux from 'reflux';
import SystemStore from "../../stores/SystemStore";
import SystemActions from "../../actions/SystemActions";
import Timer from "./Timer";
import LoadingSpinner from "./ProbesList";

class Metrics extends Reflux.Component {

    constructor(props) {
        super(props);

        this.store = SystemStore;

        this.state = {
            systemMetrics: undefined
        };
    }

    componentDidMount() {
        SystemActions.getMetrics();
        setInterval(function () {
            SystemActions.getMetrics();
        }, 5000);
    }

    render() {
        if (!this.state.systemMetrics) {
            return <LoadingSpinner/>;
        } else {
            return (
                <div>
                    <div className="row">
                        <div className="col-md-12">
                            <h3>Metrics</h3>

                            <Timer title="Frame Processing Timer" timer={this.state.systemMetrics.frame_timing}/>
                        </div>
                    </div>
                </div>
            )
        }
    }

}

export default Metrics;