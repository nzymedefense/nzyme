import React from "react";
import {useParams} from "react-router-dom";
import ApiRoutes from "../../../util/ApiRoutes";
import AlphaFeatureAlert from "../../shared/AlphaFeatureAlert";

export default function HostnameDetailsPage() {

  const { hostnameParam } = useParams()

  return (
      <React.Fragment>
        <AlphaFeatureAlert />

        <div className="row">
          <div className="col-md-12">
            <nav aria-label="breadcrumb">
              <ol className="breadcrumb">
                <li className="breadcrumb-item"><a href={ApiRoutes.ETHERNET.OVERVIEW}>Ethernet</a></li>
                <li className="breadcrumb-item"><a href={ApiRoutes.ETHERNET.L4.OVERVIEW}>Layer 4</a></li>
                <li className="breadcrumb-item">Hostnames</li>
                <li className="breadcrumb-item active" aria-current="page">{hostnameParam}</li>
              </ol>
            </nav>
          </div>
        </div>

        <div className="row">
          <div className="col-md-12">
            <h1>
              Hostname &quot;{hostnameParam}&quot;
            </h1>
          </div>
        </div>

        <div className="row">
          <div className="col-md-12">
            <div className="card">
              <div className="card-body">
                This page will show all details about the hostname, including which DNS requests and responses
                it appeared in and which clients/servers were involved.
              </div>
            </div>
          </div>
        </div>

      </React.Fragment>
  )

}