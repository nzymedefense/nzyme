import React from "react";
import Consequence from "../Consequence";
import TapDropProcedure from "./procedures/TapDropProcedure";

function TapDropConsequence(props) {

  if (!props.show) {
    return null
  }

  return (
      <Consequence
          indicator="Tap Drop"
          color="red"
          problem="One or more taps are dropping packets on the capture handle."
          acceptableRange={[
            "n/a"
          ]}
          consequences={[
            "Tap will miss data",
            "Reduced alerting and visibility abilities"
          ]}
          procedure={<TapDropProcedure />}
      />
  )

}

export default TapDropConsequence;