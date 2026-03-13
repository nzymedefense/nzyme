import React from "react";
import {useParams} from "react-router-dom";
import ApiRoutes from "../../../../util/ApiRoutes";
import usePageTitle from "../../../../util/UsePageTitle";

export default function IPDetailsPage() {

  // TODO replace with actual IP after we loaded it
  usePageTitle("IP Address Details");

  const { address } = useParams()

  return (
      <React.Fragment>
        <div className="row">
          <div className="col-md-12">
            <nav aria-label="breadcrumb">
              <ol className="breadcrumb">
                <li className="breadcrumb-item"><a href={ApiRoutes.ETHERNET.OVERVIEW}>Ethernet</a></li>
                <li className="breadcrumb-item">IP</li>
                <li className="breadcrumb-item">Addresses</li>
                <li className="breadcrumb-item active" aria-current="page">{address}</li>
              </ol>
            </nav>
          </div>
        </div>

        <div className="row">
          <div className="col-md-12">
            <h1>
              IP Address &quot;{address}&quot;
            </h1>
          </div>
        </div>

        <div className="row">
          <div className="col-md-12">
            <div className="card">
              <div className="card-body">
                This page will show all details about the IP address, including an overview of where it connected
                to, using which services and protocols.
              </div>
            </div>
          </div>
        </div>

      </React.Fragment>
  )

}