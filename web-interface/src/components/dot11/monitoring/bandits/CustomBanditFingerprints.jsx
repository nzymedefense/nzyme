import React from "react";

function CustomBanditFingerprints(props) {

  const fingerprints = props.fingerprints;
  const onDelete = props.onDelete;

  if (!fingerprints || fingerprints.length === 0) {
    return <div className="alert alert-info mb-0">This bandit has no fingerprints.</div>
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
        {fingerprints.map((f, i) => {
          return (
              <tr key={"fp-" + i}>
                <td>{f}</td>
                <td>
                  <a href="#" onClick={(e) => onDelete(e, f)}>Delete</a>
                </td>
              </tr>
          )
        })}
        </tbody>
      </table>
  )

}

export default CustomBanditFingerprints;