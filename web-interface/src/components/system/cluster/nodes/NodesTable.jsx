import React from "react";
import LoadingSpinner from "../../../misc/LoadingSpinner";

function NodesTable(props) {

  if (!props.nodes) {
    return <LoadingSpinner />
  }

  return (
      <div className="row">
        <div className="col-md-12">
          <table className="table table-sm table-hover table-striped">
            <thead>
            <tr>
              <th>Name</th>
              <th>CPU Load</th>
              <th>Memory Used</th>
              <th>Heap Used</th>
              <th>Uptime</th>
            </tr>
            </thead>
            <tbody>
            </tbody>
          </table>
        </div>
      </div>
  )

}

export default NodesTable