import React from "react";

function LoginFailedMessage(props) {

  const show = props.show;
  const message = props.message;

  if (show) {
    return <div className="alert alert-warning">{message}</div>
  }

  return null;

}

export default LoginFailedMessage;