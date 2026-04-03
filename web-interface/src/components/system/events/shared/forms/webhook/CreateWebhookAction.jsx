import React from "react";
import EventActionsService from "../../../../../../services/EventActionsService";
import {toast} from "react-toastify";
import WebhookActionForm from "./WebhookActionForm";

const eventActionsService = new EventActionsService();

export default function CreateWebhookAction(props) {

  const setComplete = props.setComplete;
  const organizationId = props.organizationId; // nullable

  const onSubmit = function(name, description, url, allowInsecure, bearerToken) {
    eventActionsService.createWebhookAction(organizationId, name, description, url, allowInsecure, bearerToken, function () {
      toast.success("Webhook action created.");
      setComplete(true);
    })
  }

  return <WebhookActionForm onSubmit={onSubmit} buttonText="Create Action" />

}