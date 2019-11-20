import React from 'react';
import Reflux from 'reflux';
import SystemStore from "../../stores/SystemStore";
import SystemActions from "../../actions/SystemActions";
import TimerRow from "./TimerRow";
import LoadingSpinner from "./ProbesList";

import numeral from "numeral";

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

                            <div className="row">
                                <div className="col-md-3">
                                    <dl>
                                        <dt>Heap Memory Usage:</dt>
                                        <dd>
                                            using {numeral(this.state.systemMetrics.mem_heap_used.value).format("0.0b")}
                                            &nbsp;of&nbsp;
                                            {numeral(this.state.systemMetrics.mem_heap_max.value).format("0.0b")}
                                            &nbsp;({numeral(this.state.systemMetrics.mem_heap_usage_percent.value).format("0.0%")})
                                        </dd>

                                        <dt>Non-Heap Memory Usage:</dt>
                                        <dd>
                                            using {numeral(this.state.systemMetrics.mem_nonheap_used.value).format("0.0b")}
                                        </dd>
                                    </dl>
                                </div>
                            </div>

                            <div className="row">
                                <div className="col-md-12">
                                    <table className="table table-sm table-hover table-striped">
                                        <thead>
                                        <tr>
                                            <th>Metric</th>
                                            <th>Maximum</th>
                                            <th>Minimum</th>
                                            <th>Mean</th>
                                            <th>99th Percentile</th>
                                            <th>Standard Deviation</th>
                                            <th>Calls</th>
                                        </tr>
                                        </thead>
                                        <tbody>
                                            <TimerRow title="802.11 Frame Processing" timer={this.state.systemMetrics.frame_timing}/>
                                            <TimerRow title="802.11 Tagged Parameter Parsing" timer={this.state.systemMetrics.tagged_params_parse_timing}/>
                                            <TimerRow title="802.11 Fingerprinting" timer={this.state.systemMetrics.tagged_params_fingerprint_timing}/>
                                            <TimerRow title="Beacon Rate Monitor Executions" timer={this.state.systemMetrics.beaconrate_monitor_timing}/>
                                            <TimerRow title="OUI Lookup" timer={this.state.systemMetrics.oui_lookup_timing}/>
                                            <TimerRow title="Signal Tables Mutex Acquisition" timer={this.state.systemMetrics.signaltables_mutex_wait}/>
                                            <TimerRow title="Signal Track Monitor Executions" timer={this.state.systemMetrics.signaltrack_monitor_timing}/>
                                        </tbody>
                                    </table>
                                </div>
                            </div>
                        </div>
                    </div>
                </div>
            )
        }
    }

}

export default Metrics;