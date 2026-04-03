import React from "react";
import EmailActionForm from "./EmailActionForm";
import {toast} from "react-toastify";
import EventActionsService from "../../../../../../services/EventActionsService";

const eventActionsService = new EventActionsService();

function CreateEmailAction(props) {

  const setComplete = props.setComplete;
  const organizationId = props.organizationId; // nullable

  const onSubmit = function(name, description, subjectPrefix, receivers) {
    eventActionsService.createEmailAction(organizationId, name, description, subjectPrefix, receivers, function () {
      toast.success('Email action created.');
      setComplete(true);
    })
  }

  return <EmailActionForm onSubmit={onSubmit} buttonText="Create Action" />

}

export default CreateEmailAction;