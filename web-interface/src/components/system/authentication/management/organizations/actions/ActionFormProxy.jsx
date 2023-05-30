import React from "react";
import SplunkActionForm from "./forms/SplunkActionForm";

function ActionFormProxy(props) {

  const type = props.type;

  if (!type) {
    return null;
  }

  switch (type) {
    case "splunk_message":
      return <SplunkActionForm />
    default:
      return "TYPE NOT IMPLEMENTED."
  }

}

export default ActionFormProxy;