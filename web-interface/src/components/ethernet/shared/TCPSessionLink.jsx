import React from "react";
import ApiRoutes from "../../../util/ApiRoutes";
import FullCopyShortenedId from "../../shared/FullCopyShortenedId";

export default function TCPSessionLink({sessionId, startTime}) {

  return (
      <a href={ApiRoutes.ETHERNET.L4.TCP.SESSION_DETAILS(sessionId, startTime)} className="machine-data">
        <FullCopyShortenedId value={sessionId} />
      </a>
  )

}