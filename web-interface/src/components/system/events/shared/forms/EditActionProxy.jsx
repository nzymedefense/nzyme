import React from "react";
import EditEmailAction from "./email/EditEmailAction";
import EditWebhookAction from "./webhook/EditWebhookAction";
import EditSyslogAction from "./syslog/EditSyslogAction";

function EditActionProxy(props) {

  const type = props.type;
  const setComplete = props.setComplete;
  const action = props.action;

  switch(type) {
    case "EMAIL":
      return <EditEmailAction action={action} setComplete={setComplete} />
    case "WEBHOOK":
      return <EditWebhookAction action={action} setComplete={setComplete} />
    case "SYSLOG":
      return <EditSyslogAction action={action} setComplete={setComplete} />
    default:
      return <span>Form not implemented.</span>
  }

}

export default EditActionProxy;