import React from "react";

export default function TimelineTapTable({taps}) {

  if (taps.length === 0) {
    return <div className="alert alert-info mb-0">No taps recorded the entity.</div>
  }

  return (
    <table className="table table-sm table-hover table-striped">
      <thead>
      <tr>
        <th>Name</th>
        <th>Location</th>
        <th>Floor</th>
        <th>Online</th>
      </tr>
      </thead>
      <tbody>
      {taps.map((tap, i) => {
        return (
          <tr key={i}>
            <td>{tap.name}</td>
            <td>{tap.location_name ? tap.location_name : <span className="text-muted">n/a</span> }</td>
            <td>{tap.floor_name ? tap.floor_name : <span className="text-muted">n/a</span> }</td>
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