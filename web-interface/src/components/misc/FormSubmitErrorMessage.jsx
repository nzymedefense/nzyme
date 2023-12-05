import React from "react";

function FormSubmitErrorMessage(props) {

  const message = props.message;

  if (!message) {
    return null;
  }

  return (
      <div className="alert alert-danger mt-3 mb-0">Error: {message}</div>
  )

}

export default FormSubmitErrorMessage;