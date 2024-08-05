import React from "react";
import {useParams} from "react-router-dom";
import AlphaFeatureAlert from "../../../shared/AlphaFeatureAlert";
import ApiRoutes from "../../../../util/ApiRoutes";

export default function SocksTunnelDetailsPage() {

  const { tunnelId } = useParams()

  return (
      <React.Fragment>
        <AlphaFeatureAlert />

        <div className="row">
          <div className="col-md-12">
            <nav aria-label="breadcrumb">
              <ol className="breadcrumb">
                <li className="breadcrumb-item"><a href={ApiRoutes.ETHERNET.OVERVIEW}>Ethernet</a></li>
                <li className="breadcrumb-item"><a href={ApiRoutes.ETHERNET.TUNNELS.INDEX}>Tunnels</a></li>
                <li className="breadcrumb-item">SOCKS</li>
                <li className="breadcrumb-item">Tunnels</li>
                <li className="breadcrumb-item active" aria-current="page">{tunnelId}</li>
              </ol>
            </nav>
          </div>
        </div>

        <div className="row">
          <div className="col-md-12">
            <h1>
              SOCKS Tunnel &quot;{tunnelId}&quot;
            </h1>
          </div>
        </div>

        <div className="row">
          <div className="col-md-12">
            <div className="card">
              <div className="card-body">
                This page will show all details about the SOCKS tunnel, including meta information of the underlying
                network layers.
              </div>
            </div>
          </div>
        </div>

      </React.Fragment>
  )

}