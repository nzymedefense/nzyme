import React from "react";

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
                        <td>{props.gauges[key].metric_value}</td>
                    </tr>
                )
            })}
            </tbody>
        </table>
    )

}

export default TapMetricsGauges;