import React, {useEffect, useState} from "react";
import StaticThresholdDetectionMethodExplanation from "../../explanations/StaticThresholdDetectionMethodExplanation";
import DiscoDetectionMethodSimulator from "../simulator/DiscoDetectionMethodSimulator";
import SimulatorButton from "../simulator/SimulatorButton";

function StaticThresholdDetectionMethodDialog(props) {

  const configuration = props.configuration;
  const monitoredNetworkId = props.monitoredNetworkId;
  const onSubmit = props.onSubmit;

  const [threshold, setThreshold] = useState("");
  const [showSimulator, setShowSimulator] = useState(false);
  const [configObject, setConfigObject] = useState(null);

  const type = "STATIC_THRESHOLD";

  useEffect(() => {
    if (configuration && configuration.method_type === type) {
      setThreshold(configuration.configuration.threshold);
    } else {
      setThreshold("10");
    }

  }, [configuration]);

  useEffect(() => {
    setShowSimulator(false);
    setConfigObject({threshold: parseInt(threshold, 10)})
  }, [threshold]);

  return (
      <React.Fragment>
        <StaticThresholdDetectionMethodExplanation />

        <h3>Configuration</h3>

        <div className="mb-3">
          <label htmlFor="threshold" className="form-label">Threshold</label>
          <input type="number"
                 className="form-control"
                 id="threshold"
                 value={threshold}
                 min={0}
                 onChange={(e) => setThreshold(e.target.value)} />
        </div>

        <SimulatorButton isToggled={showSimulator} setToggled={setShowSimulator} />{' '}

        <button className="btn btn-primary" onClick={(e) => onSubmit(e, type, configObject)}>
          Save Configuration
        </button>

        <DiscoDetectionMethodSimulator show={showSimulator}
                                       method={type}
                                       monitoredNetworkId={monitoredNetworkId}
                                       configuration={configObject} />
      </React.Fragment>
  )

}

export default StaticThresholdDetectionMethodDialog;