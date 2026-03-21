import React from "react";
import LoadingSpinner from "../../misc/LoadingSpinner";

export default function MonitorsTable({monitors, page, setPage, perPage}) {

  if (!monitors) {
    return <LoadingSpinner />
  }

  if (monitors.monitors.length === 0) {
    return <div className="alert alert-info mb-0">No monitors configured.</div>
  }

  return (
    <>
      Table
    </>
  )

}