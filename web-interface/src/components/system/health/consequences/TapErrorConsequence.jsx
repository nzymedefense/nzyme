import React from "react";
import Consequence from "../Consequence";
import TapErrorProcedure from "./procedures/TapErrorProcedure";

function TapErrorConsequence(props) {

  if (!props.show) {
    return null
  }

  return (
      <Consequence
          indicator="Tap Error"
          color="red"
          problem="One or more taps are throwing processing errors. This means they have trouble processing certain packets."
          acceptableRange={[
            "n/a"
          ]}
          consequences={[
            "Tap will miss data",
            "Reduced alerting and visibility abilities"
          ]}
          procedure={<TapErrorProcedure />}
      />
  )

}

export default TapErrorConsequence;