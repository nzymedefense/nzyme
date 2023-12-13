import React from "react";

function MonitoredFingerprintsTable(props) {
  const fingerprints = props.fingerprints;
  const onDelete = props.onDelete;

  if (!fingerprints || fingerprints.length === 0) {
    return (
        <div className="alert alert-info">
          No fingerprints are configured for this BSSID yet.
        </div>
    )
  }

  return (
      <table className="table table-sm table-hover table-striped">
        <thead>
        <tr>
          <th>Fingerprint</th>
          <th>&nbsp;</th>
        </tr>
        </thead>
        <tbody>
        {fingerprints.map(function(fingerprint, i) {
          return (
              <tr key={"fingerprint-" + i}>
                <td>{fingerprint.fingerprint}</td>
                <td><a href="#" onClick={() => onDelete(fingerprint.uuid)}>Delete</a></td>
              </tr>
          )
        })}
        </tbody>
      </table>
  )

}

export default MonitoredFingerprintsTable;