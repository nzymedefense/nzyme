import React from "react";
import EmailActionForm from "./EmailActionForm";
import EventActionsService from "../../../../../../../../services/EventActionsService";
import {notify} from "react-notify-toast";

const eventActionsService = new EventActionsService();

function CreateEmailAction(props) {

  const setComplete = props.setComplete;
  const organizationId = props.organizationId;

  const onSubmit = function(name, description, subjectPrefix, receivers) {
    eventActionsService.createEmailAction(organizationId, name, description, subjectPrefix, receivers, function() {
      notify.show('Email action created.', 'success');
      setComplete(true);
    })
  }

  return <EmailActionForm onSubmit={onSubmit} />

}

export default CreateEmailAction;