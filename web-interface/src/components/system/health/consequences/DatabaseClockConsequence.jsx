import React from "react";
import Consequence from "../Consequence";
import DatabaseClockProcedure from "./procedures/DatabaseClockPrecedure";

function DatabaseClockConsequence(props) {

  if (!props.show) {
    return null
  }

  return (
      <Consequence
          indicator="DB Clock"
          color="red"
          problem="The database server clock is not synchronized with reference world time."
          acceptableRange={[
            String.fromCharCode(177) + "5000 ms of drift from reference world time"
          ]}
          consequences={[
            "Nodes will not reliably participate in cluster task work queue processing",
            "Nodes will not reliably be detected as online or offline",
            "Nodes will not reliably perform retention cleaning of some data",
            "Potential loss of data integrity"
          ]}
          procedure={<DatabaseClockProcedure />}
      />
  )

}

export default DatabaseClockConsequence;