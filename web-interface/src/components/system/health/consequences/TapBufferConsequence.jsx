import React from "react";
import Consequence from "../Consequence";
import TapBufferProcedure from "./procedures/TapBufferProcedure";

function TapBufferConsequence(props) {

  if (!props.show) {
    return null
  }

  return (
      <Consequence
          indicator="Tap Buffer"
          color="red"
          problem="One or more taps are at risk of running out of buffer space. Taps are using buffers to asynchronously
          process packets and data is dropped if a buffer is completely full."
          acceptableRange={[
            "0-75% buffer usage per buffer"
          ]}
          consequences={[
            "Tap will potentially miss data",
            "Potentially reduced alerting and visibility abilities"
          ]}
          procedure={<TapBufferProcedure />}
      />
  )

}

export default TapBufferConsequence;