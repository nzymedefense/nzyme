import React from "react";
import numeral from "numeral";
import FormattedGauge from "./FormattedGauge";

function TapMetricsGauges(props) {

    return (
        <table className="table table-sm table-hover table-striped">
            <thead>
            <tr>
                <th>Gauge</th>
                <th>Value</th>
            </tr>
            </thead>
            <tbody>
            {Object.keys(props.gauges).sort((a, b) => a.localeCompare(b)).map(function (key, i) {
                return (
                    <tr key={"metric-gauge-" + i}>
                        <td>{props.gauges[key].metric_name}</td>
                        <td><FormattedGauge name={props.gauges[key].metric_name} value={props.gauges[key].metric_value} /></td>
                    </tr>
                )
            })}
            </tbody>
        </table>
    )

}

export default TapMetricsGauges;