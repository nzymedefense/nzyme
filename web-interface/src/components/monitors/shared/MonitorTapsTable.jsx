import React from "react";

export default function MonitorTapsTable({allTaps, selectedTaps}) {

  if (selectedTaps === null || (selectedTaps.length === 1 && selectedTaps[0] === "*")) {
    return <div className="alert alert-info mb-0">This monitor will always use data from all your taps.</div>
  }

  return (
    <table className="table table-sm table-hover table-striped mt-0">
      <thead>
      <tr>
        <th>Name</th>
        <th>Online</th>
      </tr>
      </thead>
      <tbody>
      {allTaps.filter(tap => selectedTaps.includes(tap.uuid)).map((tap, i) => {
        return (
          <tr key={i}>
            <td>{tap.name}</td>
            <td>{tap.is_online ?
              <span className="text-success">Online</span>
              : <span className="text-warning">Offline</span>}
            </td>
          </tr>
        )
      })}
      </tbody>
    </table>
  )

}