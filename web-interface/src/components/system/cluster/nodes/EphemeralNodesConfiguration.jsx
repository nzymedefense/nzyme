import React, {useEffect, useState} from "react";
import ConfigurationValue from "../../../configuration/ConfigurationValue";
import ConfigurationModal from "../../../configuration/modal/ConfigurationModal";
import LoadingSpinner from "../../../misc/LoadingSpinner";
import ClusterService from "../../../../services/ClusterService";

const clusterService = new ClusterService()

function EphemeralNodesConfiguration(props) {

  const [configuration, setConfiguration] = useState(null)
  const [localRevision, setLocalRevision] = useState(0)

  useEffect(() => {
    clusterService.findNodesConfiguration(setConfiguration)
  }, [localRevision])

  if (!configuration) {
    return <LoadingSpinner />
  }

  return (
      <React.Fragment>
        <div className="row">
          <div className="col-md-12">
            <h3>Ephemeral Nodes</h3>

            <p>
              Ephemeral nodes are nzyme nodes that are short-lived. This is a common pattern for immutable deployments
              or highly flexible, container-based architectures in which nodes can be destroyed and created very rapidly.
            </p>

            <p>
              Nzyme treats ephemeral nodes differently from standard nodes. For example, an ephemeral node will not trigger
              an alert when it goes offline.
            </p>

            <p>
              Every node with a name that matches the regular expression configured below will be considered ephemeral.
            </p>
          </div>
        </div>

        <div className="row">
          <div className="col-md-12">
            <table className="table table-sm table-hover table-striped">
              <thead>
              <tr>
                <th>Configuration</th>
                <th>Value</th>
                <th>Actions</th>
              </tr>
              </thead>
              <tbody>
              <tr>
                <td>Ephemeral Node Name Regular Expression</td>
                <td>
                  <ConfigurationValue value={configuration.ephemeral_nodes_regex.value}
                                      configKey={configuration.ephemeral_nodes_regex.key} />
                </td>
                <td>
                  <ConfigurationModal config={configuration.ephemeral_nodes_regex}
                                      setGlobalConfig={setConfiguration}
                                      setLocalRevision={setLocalRevision}
                                      dbUpdateCallback={clusterService.updateNodesConfiguration} />
                </td>
              </tr>
              </tbody>
            </table>
          </div>
        </div>
      </React.Fragment>
  )

}

export default EphemeralNodesConfiguration;