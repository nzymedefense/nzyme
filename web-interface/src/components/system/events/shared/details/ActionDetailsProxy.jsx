import React from "react";
import EmailActionDetails from "./email/EmailActionDetails";
import WebhookActionDetails from "./webhook/WebhookActionDetails";
import SyslogActionDetails from "./syslog/SyslogActionDetails";

function ActionDetailsProxy(props) {

  const action = props.action;

  switch(action.action_type) {
    case "EMAIL":
      return <EmailActionDetails action={action} />
    case "WEBHOOK":
      return <WebhookActionDetails action={action} />
    case "SYSLOG":
      return <SyslogActionDetails action={action} />
    default:
      return <span>Not supported: {action.action_type}</span>;
  }

}

export default ActionDetailsProxy;