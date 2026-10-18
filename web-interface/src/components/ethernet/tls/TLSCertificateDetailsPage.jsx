import React from "react";
import {useParams} from "react-router-dom";
import ApiRoutes from "../../../util/ApiRoutes";
import usePageTitle from "../../../util/UsePageTitle";

export default function TLSCertificateDetailsPage() {

  usePageTitle("TLS Certificate Details");

  const { sha256 } = useParams()

  return (
      <React.Fragment>
        <div className="row">
          <div className="col-md-12">
            <nav aria-label="breadcrumb">
              <ol className="breadcrumb">
                <li className="breadcrumb-item"><a href={ApiRoutes.ETHERNET.OVERVIEW}>Ethernet</a></li>
                <li className="breadcrumb-item">TLS</li>
                <li className="breadcrumb-item"><a href={ApiRoutes.ETHERNET.TLS.CERTIFICATES.INDEX}>Certificates</a></li>
                <li className="breadcrumb-item active" aria-current="page">{sha256}</li>
              </ol>
            </nav>
          </div>
        </div>

        <div className="row mt-3">
          <div className="col-md-12">
            <h1>
              Certificate &quot;{sha256}&quot;
            </h1>
          </div>
        </div>

        <div className="row mt-3">
          <div className="col-md-12">
            <div className="card">
              <div className="card-body">
                <div className="mb-0 alert alert-info">
                  Certificate details are not implemented yet.
                </div>
              </div>
            </div>
          </div>
        </div>
      </React.Fragment>
  )

}
