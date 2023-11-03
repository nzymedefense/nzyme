import React, {useEffect, useState} from "react";
import Dot11Service from "../../../../services/Dot11Service";
import LoadingSpinner from "../../../misc/LoadingSpinner";
import NoOpDetectionMethodDetails from "./details/NoOpDetectionMethodDetails";
import StaticThresholdDetectionMethodDetails from "./details/StaticThresholdDetectionMethodDetails";

const dot11Service = new Dot11Service();

function DiscoDetectionDetails(props) {

  const monitoredNetwork = props.monitoredNetwork;

  const [configuration, setConfiguration] = useState(null);

  useEffect(() => {
    dot11Service.getDiscoDetectionConfiguration(monitoredNetwork.uuid, setConfiguration);
  }, [monitoredNetwork]);

  if (!configuration) {
    return <LoadingSpinner />
  }

  switch (configuration.method_type) {
    case "NOOP":
      return <NoOpDetectionMethodDetails />
    case "STATIC_THRESHOLD":
      return <StaticThresholdDetectionMethodDetails configuration={configuration.configuration} />
    default:
      return <span>Detection method type &quot;{configuration.method_type}&quot; not implemented.</span>
  }

}

export default DiscoDetectionDetails;