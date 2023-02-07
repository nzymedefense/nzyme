import React, {useState} from "react";
import LoadingSpinner from "../../../misc/LoadingSpinner";
import NodeRow from "./NodeRow";

function NodesTable(props) {

  const nodes = props.nodes

  const [showOfflineNodes, setShowOfflineNodes] = useState(false)

  const updateOfflineNodesSelection = function(e) {
    setShowOfflineNodes(e.target.checked)
  }

  if (!nodes) {
    return <LoadingSpinner />
  }

  return (
      <div className="row">
        <div className="col-md-12">

          <h3 style={{display: "inline-block"}}>All Nodes</h3>

          <div className="form-check form-switch float-end">
            <input className="form-check-input" type="checkbox" role="switch"
                   id="showOfflineNodes"onChange={updateOfflineNodesSelection} />
            <label className="form-check-label" htmlFor="showOfflineNodes">
              Show recently active but offline nodes
            </label>
          </div>

          <table className="table table-sm table-hover table-striped">
            <thead>
            <tr>
              <th>Name</th>
              <th>CPU Load</th>
              <th>Memory Used</th>
              <th>Heap Used</th>
              <th>Process Size</th>
              <th>Clock</th>
              <th>Version</th>
            </tr>
            </thead>
            <tbody>
            {Object.keys(nodes.sort((a, b) => a.name.localeCompare(b.name))).map(function (key, i) {
              return <NodeRow key={'node-' + i} node={nodes[i]} showOfflineNodes={showOfflineNodes} />
            })}
            </tbody>
          </table>
        </div>
      </div>
  )

}

export default NodesTable
