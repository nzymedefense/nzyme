import React from "react";
import LoadingSpinner from "../../../../../misc/LoadingSpinner";
import moment from "moment";
import numeral from "numeral";

function AnomaliesTable(props) {

  const anomalies = props.anomalies;

  if (!anomalies) {
    return <LoadingSpinner />;
  }

  if (anomalies.anomalies.length === 0) {
    return <div className="alert alert-info mb-0">No anomalies detected.</div>
  }

  return (
      <React.Fragment>
        <table className="table table-sm table-hover table-striped mb-3">
          <thead>
          <tr>
            <th>Timestamp</th>
            <th>Frames</th>
          </tr>
          </thead>
          <tbody>
          {anomalies.anomalies.map((anomaly, i) => {
            return (
                <tr key={i}>
                  <td>{moment(anomaly.timestamp).format()}</td>
                  <td>{numeral(anomaly.frame_count).format("0,0")}</td>
                </tr>
            )
          })}
          </tbody>
        </table>
      </React.Fragment>
  )

}

export default AnomaliesTable;