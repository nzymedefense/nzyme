import React from "react";
import EmailActionForm from "./EmailActionForm";
import EventActionsService from "../../../../../../services/EventActionsService";
import {toast} from "react-toastify";

const eventActionsService = new EventActionsService();

export default function EditEmailAction(props) {

  const setComplete = props.setComplete;
  const action = props.action;

  const onSubmit = function(name, description, subjectPrefix, receivers) {
    eventActionsService.updateEmailAction(action.id, name, description, subjectPrefix, receivers, function() {
      toast.success('Action updated.');
      setComplete(true);
    })
  }

  return <EmailActionForm action={action} onSubmit={onSubmit} buttonText={"Update Action"} />
}