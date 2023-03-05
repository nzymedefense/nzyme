import React, {useState} from 'react'
import LoadingSpinner from '../../../misc/LoadingSpinner'
import PGPMetricsTable from "./PGPMetricsTable";

function PGPMetrics (props) {
  const [showNodes, setShowNodes] = useState(false)

  if (!props.metrics) {
    return <LoadingSpinner />
  }

  const updateNodeDetailsSelection = function(e) {
    setShowNodes(e.target.checked)
  }

  return (
      <React.Fragment>
        <div className="form-check form-switch float-end">
          <input className="form-check-input" type="checkbox" role="switch"
                 id="showOfflineNodes" onChange={updateNodeDetailsSelection} />
          <label className="form-check-label" htmlFor="showOfflineNodes">
            Expand Individual Node Metrics
          </label>
        </div>

        <PGPMetricsTable metrics={props.metrics} showNodes={showNodes} />
      </React.Fragment>
  )
}

export default PGPMetrics
