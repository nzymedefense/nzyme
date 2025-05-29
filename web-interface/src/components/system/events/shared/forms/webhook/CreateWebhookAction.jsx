import React from "react";
import {notify} from "react-notify-toast";
import EventActionsService from "../../../../../../services/EventActionsService";
import WebhookActionForm from "./WebhookActionForm";

const eventActionsService = new EventActionsService();

export default function CreateWebhookAction(props) {

  const setComplete = props.setComplete;
  const organizationId = props.organizationId; // nullable

  const onSubmit = function(name, description, subjectPrefix, receivers) {
    /*eventActionsService.createEmailAction(organizationId, name, description, subjectPrefix, receivers, function () {
      notify.show("Webhook action created.", "success");
      setComplete(true);
    })*/
  }

  return <WebhookActionForm onSubmit={onSubmit} buttonText="Create Action" />

}