import React from "react";

function InlineFormValidationMessage(props) {

  const message = props.message;

  if (!message) {
    return null;
  }

  return (
      <span className="text-danger">{message}</span>
  )

}

export default InlineFormValidationMessage;