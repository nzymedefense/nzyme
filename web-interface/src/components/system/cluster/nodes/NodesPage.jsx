import React, {useEffect, useState} from "react";
import ClusterService from "../../../../services/ClusterService";
import NodesTable from "./NodesTable";
import EphemeralNodesConfiguration from "./EphemeralNodesConfiguration";

const clusterService = new ClusterService()

function fetchData(setNodes) {
  clusterService.findAllNodes(setNodes)
}

function NodesPage() {

  const [nodes, setNodes] = useState(null)

  useEffect(() => {
    fetchData(setNodes,)
    const id = setInterval(() => fetchData(setNodes), 5000)
    return () => clearInterval(id)
  }, [setNodes])

  return (
      <div>
        <div className="row">
          <div className="col-md-12">
            <h1>Cluster &amp; Nodes</h1>
          </div>
        </div>

        <div className="row mt-3">
          <div className="col-md-6">
            <div className="card">
              <div className="card-body">
                <EphemeralNodesConfiguration />
              </div>
            </div>
          </div>
        </div>

        <div className="row mt-3">
          <div className="col-md-12">
            <div className="card">
              <div className="card-body">
                <NodesTable nodes={nodes} />
              </div>
            </div>
          </div>
        </div>
      </div>
  )

}

export default NodesPage
