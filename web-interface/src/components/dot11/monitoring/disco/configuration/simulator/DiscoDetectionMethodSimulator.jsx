import React, {useEffect, useState} from "react";
import LoadingSpinner from "../../../../../misc/LoadingSpinner";
import TapsService from "../../../../../../services/TapsService";
import InlineTapSelector from "../../../../../shared/InlineTapSelector";
import SimulatorChart from "./SimulatorChart";


function DiscoDetectionMethodSimulator(props) {

  const show = props.show;

  const method = props.method;
  const configuration = props.configuration;
  const monitoredNetworkId = props.monitoredNetworkId;

  const [selectedTapUuid, setSelectedTapUuid] = useState(null);

  if (!show) {
    return null
  }

  return (
      <div className="mt-4">
        <h3>Detection Simulator</h3>

        <InlineTapSelector onTapSelected={(tapUuid) => setSelectedTapUuid(tapUuid)} />

        <SimulatorChart selectedTapUuid={selectedTapUuid}
                        methodType={method}
                        configuration={configuration}
                        monitoredNetworkId={monitoredNetworkId} />
      </div>
  )

}

export default DiscoDetectionMethodSimulator;