import React from "react";
import EventActionsService from "../../../../../../services/EventActionsService";
import {notify} from "react-notify-toast";
import WebhookActionForm from "./WebhookActionForm";

const eventActionsService = new EventActionsService();

export default function EditWebhookAction(props) {

  const setComplete = props.setComplete;
  const action = props.action;

  const onSubmit = function(name, description,  url, allowInsecure, bearerToken) {
    eventActionsService.updateWebhookAction(action.id, name, description,  url, allowInsecure, bearerToken, function() {
      notify.show('Action updated.', 'success');
      setComplete(true);
    })
  }

  return <WebhookActionForm action={action} onSubmit={onSubmit} buttonText={"Update Action"} />

}