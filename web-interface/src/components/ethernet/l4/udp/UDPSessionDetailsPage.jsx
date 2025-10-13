import {useParams} from "react-router-dom";
import ApiRoutes from "../../../../util/ApiRoutes";
import CardTitleWithControls from "../../../shared/CardTitleWithControls";
import React from "react";

export default function UDPSessionDetailsPage() {

  const {sessionId} = useParams();

  return (
      <React.Fragment>
        <div className="row">
          <div className="col-md-12">
            <nav aria-label="breadcrumb">
              <ol className="breadcrumb">
                <li className="breadcrumb-item"><a href={ApiRoutes.ETHERNET.OVERVIEW}>Ethernet</a></li>
                <li className="breadcrumb-item">TCP/UDP</li>
                <li className="breadcrumb-item"><a href={ApiRoutes.ETHERNET.L4.OVERVIEW}>Sessions</a></li>
                <li className="breadcrumb-item">UDP</li>
                <li className="breadcrumb-item active" aria-current="page">{sessionId}</li>
              </ol>
            </nav>
          </div>
        </div>

        <div className="row mt-3">
          <div className="col-md-12">
            <h1>
              UDP Session &quot;{sessionId}&quot;
            </h1>
          </div>
        </div>

        <div className="row mt-3">
          <div className="col-md-4">
            <div className="card">
              <div className="card-body">
                <CardTitleWithControls title="Details" />
              </div>
            </div>
          </div>
        </div>
      </React.Fragment>
  )

}