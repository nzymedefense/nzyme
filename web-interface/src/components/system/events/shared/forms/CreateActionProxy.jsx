import React from "react";
import CreateEmailAction from "../forms/email/CreateEmailAction";
import CreateWebhookAction from "./webhook/CreateWebhookAction";

function CreateActionProxy(props) {

  const organizationId = props.organizationId;
  const type = props.type;
  const setComplete = props.setComplete;

  if (!type) {
    return null;
  }

  switch (type) {
    case "email":
      return <CreateEmailAction setComplete={setComplete} organizationId={organizationId} />
    case "webhook":
      return <CreateWebhookAction setComplete={setComplete} organizationId={organizationId} />
    default:
      return "TYPE NOT IMPLEMENTED."
  }

}

export default CreateActionProxy;