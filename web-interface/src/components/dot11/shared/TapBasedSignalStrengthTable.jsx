import React from "react";
import SignalStrength from "../util/SignalStrength";

function TapBasedSignalStrengthTable(props) {

  const strengths = props.strengths;

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
              <tr>
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