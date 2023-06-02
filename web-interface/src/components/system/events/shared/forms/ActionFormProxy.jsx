import React from "react";
import EmailActionForm from "./email/EmailActionForm";

function ActionFormProxy(props) {

  const type = props.type;
  const action = props.action;
  const buttonText = props.buttonText;
  const onSubmit = props.onSubmit;

  switch(type) {
    case "EMAIL":
      return <EmailActionForm action={action} onSubmit={onSubmit} buttonText={buttonText} />
    default:
      return <span>Form not implemented.</span>
  }

}

export default ActionFormProxy;