import React from "react";

export default function SSHSessionKey(props) {

  const sessionKey = props.sessionKey;

  return (
      <span className="ssh-session-key" title={sessionKey}>
        {sessionKey.substring(0, 6).toUpperCase()}
      </span>
  )

}