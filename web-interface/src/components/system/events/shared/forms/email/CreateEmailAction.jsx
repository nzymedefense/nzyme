import React from "react";
import EmailActionForm from "./EmailActionForm";
import {notify} from "react-notify-toast";
import EventActionsService from "../../../../../../services/EventActionsService";

const eventActionsService = new EventActionsService();

function CreateEmailAction(props) {

  const setComplete = props.setComplete;
  const organizationId = props.organizationId; // nullable

  const onSubmit = function(name, description, subjectPrefix, receivers) {
    eventActionsService.createEmailAction(organizationId, name, description, subjectPrefix, receivers, function () {
      notify.show('Email action created.', 'success');
      setComplete(true);
    })
  }

  return <EmailActionForm onSubmit={onSubmit} buttonText="Create Action" />

}

export default CreateEmailAction;