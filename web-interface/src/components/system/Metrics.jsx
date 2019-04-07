import React from 'react';
import Reflux from 'reflux';
import SystemStore from "../../stores/SystemStore";
import SystemActions from "../../actions/SystemActions";
import TimerRow from "./TimerRow";
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

                            <table className="table table-sm table-hover table-striped">
                                <thead>
                                <tr>
                                    <th>Metric</th>
                                    <th>Maximum</th>
                                    <th>Minimum</th>
                                    <th>Mean</th>
                                    <th>99th Percentile</th>
                                    <th>Standard Deviation</th>
                                </tr>
                                </thead>
                                <tbody>
                                    <TimerRow title="Frame Processing Timer" timer={this.state.systemMetrics.frame_timing}/>
                                </tbody>
                            </table>
                        </div>
                    </div>
                </div>
            )
        }
    }

}

export default Metrics;