import React from "react";
import StaticThresholdDetectionMethodExplanation from "../explanations/StaticThresholdDetectionMethodExplanation";

function StaticThresholdDetectionMethodDetails(props) {

  const configuration = props.configuration;

  return (
      <React.Fragment>
        <h4>Method: Static Threshold</h4>

        <StaticThresholdDetectionMethodExplanation />

        <h4>Configuration</h4>

        <dl>
          <dt>Threshold</dt>
          <dd>{configuration.threshold}</dd>
        </dl>
      </React.Fragment>
  )

}

export default StaticThresholdDetectionMethodDetails;