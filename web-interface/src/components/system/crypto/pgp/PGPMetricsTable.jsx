import React from "react";
import TimerRow from "../../../misc/metrics/TimerRow";

function PGPMetricsTable(props) {

  if (props.showNodes) {
    const nodeMetrics = props.metrics.node_metrics
    return (
        <table className="table table-sm table-hover table-striped">
          <thead>
          <tr>
            <th>Metric</th>
            <th>Max</th>
            <th>Min</th>
            <th>Mean</th>
            <th>P99</th>
            <th>StdDev</th>
            <th>Calls</th>
          </tr>
          </thead>
          <tbody>
          {Object.keys(nodeMetrics).map(function (key, i) {
            return (
              <React.Fragment key={"pgpmetrics-" + key}>
                <tr className="table-span-heading">
                  <td colSpan={7}>{key}</td>
                </tr>
                <TimerRow title="Encryption Operations" timer={nodeMetrics[key].pgp_encryption_timer}/>
                <TimerRow title="Decryption Operations" timer={nodeMetrics[key].pgp_decryption_timer}/>
              </React.Fragment>
            );
          })}
          </tbody>
        </table>
    )
  } else {
    return (
        <table className="table table-sm table-hover table-striped">
          <thead>
          <tr>
            <th>Metric</th>
            <th>Max</th>
            <th>Min</th>
            <th>Mean</th>
            <th>P99</th>
            <th>StdDev</th>
            <th>Calls</th>
          </tr>
          </thead>
          <tbody>
          <TimerRow title="Encryption Operations" timer={props.metrics.cluster_metrics.pgp_encryption_timer}/>
          <TimerRow title="Decryption Operations" timer={props.metrics.cluster_metrics.pgp_decryption_timer}/>
          </tbody>
        </table>
    )
  }

}

export default PGPMetricsTable;
