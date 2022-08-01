import React from "react";
import TapMetricsGauges from "./TapMetricsGauges";

function TapMetrics(props) {

    return (
        <div>
            <div className="row mt-3">
                <div className="col-md-12">
                    <h6>Gauges</h6>

                    <TapMetricsGauges gauges={props.metrics.gauges} />
                </div>
            </div>
        </div>
    )

}

export default TapMetrics;