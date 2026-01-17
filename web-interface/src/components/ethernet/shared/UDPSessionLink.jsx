import React from "react";
import ApiRoutes from "../../../util/ApiRoutes";
import FullCopyShortenedId from "../../shared/FullCopyShortenedId";

export default function UDPSessionLink({sessionId, startTime}) {

  return (
    <a href={ApiRoutes.ETHERNET.L4.UDP.SESSION_DETAILS(sessionId, startTime)} className="machine-data">
      <FullCopyShortenedId value={sessionId} />
    </a>
  )

}