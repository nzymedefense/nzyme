import React from "react";
import EmailActionDetails from "./email/EmailActionDetails";

function ActionDetailsProxy(props) {

  const action = props.action;

  switch(action.action_type) {
    case "EMAIL":
      return <EmailActionDetails action={action} />
    default:
      return <span>Not supported: {action.action_type}</span>;
  }

}

export default ActionDetailsProxy;