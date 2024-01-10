import React from "react";
import SignalStrength from "../util/SignalStrength";

function TapBasedSignalStrengthTable(props) {

  const strengths = props.strengths;

  if (!strengths || strengths.length === 0) {
    return (
        <div className="alert alert-info mb-0">
          No data recorded in requested time frame.
        </div>
    )
  }

  return (
      <table className="table table-sm table-hover table-striped mb-0">
        <thead>
        <tr>
          <th>Tap</th>
          <th>Signal Strength</th>
        </tr>
        </thead>
        <tbody>
        {strengths.map((s, i) => {
          return (
              <tr key={i}>
                <td>{s.tap_name}</td>
                <td><SignalStrength strength={s.signal_strength} /></td>
              </tr>
          )
        })}
        </tbody>
      </table>
  )

}

export default TapBasedSignalStrengthTable;