import React from "react";
import EventActionsService from "../../../../../../services/EventActionsService";
import {toast} from "react-toastify";
import SyslogActionForm from "./SyslogActionForm";

const eventActionsService = new EventActionsService();

export default function EditSyslogAction(props) {

  const setComplete = props.setComplete;
  const action = props.action;

  const onSubmit = function(name, description, protocol, syslogHostname, host, port) {
    eventActionsService.updateSyslogAction(action.id, name, description, protocol, syslogHostname, host, port, function() {
      toast.success('Action updated.');
      setComplete(true);
    })
  }

  return <SyslogActionForm action={action} onSubmit={onSubmit} buttonText={"Update Action"} />

}