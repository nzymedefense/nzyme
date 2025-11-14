import React from "react";
import {useParams} from "react-router-dom";
import ApiRoutes from "../../../../util/ApiRoutes";
import SSHSessionDetails from "./SSHSessionDetails";

export default function SSHSessionDetailsPage() {

  const { sessionId } = useParams()

  return (
      <React.Fragment>
        <div className="row">
          <div className="col-md-12">
            <nav aria-label="breadcrumb">
              <ol className="breadcrumb">
                <li className="breadcrumb-item"><a href={ApiRoutes.ETHERNET.OVERVIEW}>Ethernet</a></li>
                <li className="breadcrumb-item">Remote Access</li>
                <li className="breadcrumb-item"><a href={ApiRoutes.ETHERNET.REMOTE.SSH.INDEX}>SSH Sessions</a></li>
                <li className="breadcrumb-item active" aria-current="page">{sessionId}</li>
              </ol>
            </nav>
          </div>
        </div>

        <div className="row mt-3">
          <div className="col-md-12">
            <h1>
              SSH Session &quot;{sessionId}&quot;
            </h1>
          </div>
        </div>

        <SSHSessionDetails sessionId={sessionId} />

      </React.Fragment>
  )

}