import React from "react";
import LoadingSpinner from "../../../misc/LoadingSpinner";
import NodeRow from "./NodeRow";

function NodesTable(props) {

  const nodes = props.nodes

  if (!nodes) {
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
              <th>Process Size</th>
              <th>Version</th>
            </tr>
            </thead>
            <tbody>
            {Object.keys(nodes.sort((a, b) => a.name.localeCompare(b.name))).map(function (key, i) {
              return <NodeRow key={'node-' + i} node={nodes[i]} />
            })}
            </tbody>
          </table>
        </div>
      </div>
  )

}

export default NodesTable