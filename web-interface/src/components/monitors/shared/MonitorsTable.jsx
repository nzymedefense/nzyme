import React from "react";
import LoadingSpinner from "../../misc/LoadingSpinner";
import Paginator from "../../misc/Paginator";
import numeral from "numeral";
import moment from "moment";

export default function MonitorsTable({monitors, page, setPage, perPage}) {

  const alertStatusIndicator = (monitor) => {
    if (!monitor.enabled) {
      return <i className="fa-solid fa-circle text-muted" title="Monitor is disabled."></i>
    } else {
      if (true) { // TODO
        return <i className="fa-solid fa-circle text-danger blink" title="Monitor is currently alerted."></i>
      } else {
        return <i className="fa-solid fa-circle text-success" title="Monitor is not currently alerted."></i>
      }
    }
  }

  if (!monitors) {
    return <LoadingSpinner />
  }

  if (monitors.monitors.length === 0) {
    return <div className="alert alert-info mb-0">No monitors configured.</div>
  }

  return (
    <>
      <table className="table table-sm table-hover table-striped">
        <thead>
        <tr>
          <th style={{width: 25}}></th>
          <th>Name</th>
          <th>Taps</th>
          <th>Interval</th>
          <th>Created At</th>
        </tr>
        </thead>
        <tbody>
        {monitors.monitors.map(function(m, i) {
          return (
            <tr key={i}>
              <td>{alertStatusIndicator(m)}</td>
              <td>{m.name}</td>
              <td>{m.taps === null ? "All" : m.taps.length}</td>
              <td>{numeral(m.interval).format("0,0")} {m.interval === 1 ? "Minute" : "Minutes"}</td>
              <td title={moment(m.created_at).format()}>{moment(m.created_at).fromNow()}</td>
            </tr>
          )
        })}
        </tbody>
      </table>

      <Paginator itemCount={monitors.total} perPage={perPage} setPage={setPage} page={page} />
    </>
  )

}