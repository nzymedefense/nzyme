import React from "react";
import EventActionsService from "../../../../../../services/EventActionsService";
import {notify} from "react-notify-toast";
import SyslogActionForm from "./SyslogActionForm";

const eventActionsService = new EventActionsService();

export default function CreateSyslogAction(props) {

  const setComplete = props.setComplete;
  const organizationId = props.organizationId; // nullable

  const onSubmit = function(name, description, protocol, syslogHostname, host, port) {
    eventActionsService.createSyslogAction(organizationId, name, description, protocol, syslogHostname, host, port, function () {
      notify.show("Syslog action created.", "success");
      setComplete(true);
    })
  }

  return <SyslogActionForm onSubmit={onSubmit} buttonText="Create Action" />

}