import React from "react";
import {useParams} from "react-router-dom";
import ApiRoutes from "../../../../util/ApiRoutes";

export default function SSHSessionDetailsPage() {

  const { sessionId } = useParams()

  return (
      <React.Fragment>
        <div className="row">
          <div className="col-md-12">
            <nav aria-label="breadcrumb">
              <ol className="breadcrumb">
                <li className="breadcrumb-item"><a href={ApiRoutes.ETHERNET.OVERVIEW}>Ethernet</a></li>
                <li className="breadcrumb-item"><a href={ApiRoutes.ETHERNET.REMOTE.INDEX}>Remote Access</a></li>
                <li className="breadcrumb-item">SSH</li>
                <li className="breadcrumb-item">Sessions</li>
                <li className="breadcrumb-item active" aria-current="page">{sessionId}</li>
              </ol>
            </nav>
          </div>
        </div>

        <div className="row">
          <div className="col-md-12">
            <h1>
              SSH Session &quot;{sessionId}&quot;
            </h1>
          </div>
        </div>

        <div className="row">
          <div className="col-md-12">
            <div className="card">
              <div className="card-body">
                This page will show all details about the SSH session, including meta information of the underlying
                network layers.
              </div>
            </div>
          </div>
        </div>

      </React.Fragment>
  )

}