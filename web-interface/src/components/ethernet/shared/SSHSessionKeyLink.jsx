import React from "react";
import ApiRoutes from "../../../util/ApiRoutes";
import SSHSessionKey from "./SSHSessionKey";

export default function SSHSessionKeyLink(props) {

  const sessionKey = props.sessionKey;

  return (
      <a href={ApiRoutes.ETHERNET.REMOTE.SSH.SESSION_DETAILS(sessionKey)}>
        <SSHSessionKey sessionKey={sessionKey} />
      </a>
  )

}