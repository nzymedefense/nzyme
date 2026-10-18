import React from "react";
import FullCopyShortenedId from "../../shared/FullCopyShortenedId";
import ApiRoutes from "../../../util/ApiRoutes";

export default function TLSSessionKey(props) {

  const sessionKey = props.sessionKey;

  return (
    <a href={ApiRoutes.ETHERNET.TLS.SESSIONS.DETAILS(sessionKey)}>
      <FullCopyShortenedId value={sessionKey} />
    </a>
  )

}
