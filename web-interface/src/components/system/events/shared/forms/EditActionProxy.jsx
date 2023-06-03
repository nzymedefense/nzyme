import React from "react";
import EditEmailAction from "./email/EditEmailAction";

function EditActionProxy(props) {

  const type = props.type;
  const setComplete = props.setComplete;
  const action = props.action;

  switch(type) {
    case "EMAIL":
      return <EditEmailAction action={action} setComplete={setComplete} />
    default:
      return <span>Form not implemented.</span>
  }

}

export default EditActionProxy;