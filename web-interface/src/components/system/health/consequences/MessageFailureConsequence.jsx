import React from "react";
import Consequence from "../Consequence";
import MessageFailureProcedure from "./procedures/MessageFailureProcedure";

function MessageFailureConsequence(props) {

  if (!props.show) {
    return null
  }

  return (
      <Consequence
          indicator="Message Failure"
          color="red"
          problem="At least one message bus message could not be processed."
          acceptableRange={[
            "No unacknowledged failed messages in the last 24 hours."
          ]}
          consequences={[
            "An expected background action of the program was not performed."
          ]}
          procedure={<MessageFailureProcedure />}
      />
  )

}

export default MessageFailureConsequence;