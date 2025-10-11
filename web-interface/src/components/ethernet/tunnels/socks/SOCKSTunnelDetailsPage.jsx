import React, {useContext, useEffect, useState} from "react";
import {useParams} from "react-router-dom";
import ApiRoutes from "../../../../util/ApiRoutes";
import CardTitleWithControls from "../../../shared/CardTitleWithControls";
import SocksService from "../../../../services/ethernet/SocksService";
import LoadingSpinner from "../../../misc/LoadingSpinner";
import useSelectedTenant from "../../../system/tenantselector/useSelectedTenant";
import {TapContext} from "../../../../App";
import GenericConnectionStatus from "../../shared/GenericConnectionStatus";
import numeral from "numeral";

const socksService = new SocksService();

export default function SOCKSTunnelDetailsPage() {

  const { tunnelId } = useParams();

  const tapContext = useContext(TapContext);
  const selectedTaps = tapContext.taps;

  const [organizationId, tenantId] = useSelectedTenant();

  const [tunnel, setTunnel] = useState(null);

  useEffect(() => {
    setTunnel(null);
    socksService.findTunnel(tunnelId, organizationId, tenantId, selectedTaps, setTunnel);
  }, [tunnelId])

  if (tunnel == null) {
    return <LoadingSpinner />
  }

  return (
      <React.Fragment>
        <div className="row">
          <div className="col-md-12">
            <nav aria-label="breadcrumb">
              <ol className="breadcrumb">
                <li className="breadcrumb-item"><a href={ApiRoutes.ETHERNET.OVERVIEW}>Ethernet</a></li>
                <li className="breadcrumb-item">Tunnels</li>
                <li className="breadcrumb-item"><a href={ApiRoutes.ETHERNET.TUNNELS.SOCKS.INDEX}>SOCKS Tunnels</a></li>
                <li className="breadcrumb-item active" aria-current="page">{tunnelId}</li>
              </ol>
            </nav>
          </div>
        </div>

        <div className="row mt-3">
          <div className="col-md-12">
            <h1>
              SOCKS Tunnel &quot;{tunnelId}&quot;
            </h1>
          </div>
        </div>

        <div className="row mt-3">
          <div className="col-md-4">
            <div className="card">
              <div className="card-body">
                <CardTitleWithControls title="Details" />

                <dl className="mb-0">
                  <dt>SOCKS Type</dt>
                  <dd>{tunnel.socks_type}</dd>
                  <dt>Authentication Status</dt>
                  <dd>{tunnel.authentication_status}</dd>
                  <dt>Handshake Status</dt>
                  <dd>{tunnel.handshake_status}</dd>
                  <dt>Connection Status</dt>
                  <dd><GenericConnectionStatus status={tunnel.connection_status} /></dd>
                  <dt>Username</dt>
                  <dd>{tunnel.username ? tunnel.username : <span className="text-muted">n/a</span>}</dd>
                </dl>
              </div>
            </div>
          </div>

          <div className="col-md-4">
            <div className="card">
              <div className="card-body">
                <CardTitleWithControls title="Tunnel Destination" />

                <dl className="mb-0">

                </dl>
              </div>
            </div>
          </div>

          <div className="col-md-4">
            <div className="card">
              <div className="card-body">
                <CardTitleWithControls title="Metadata" />

                <dl className="mb-0">
                  <dt>Tunneled Bytes</dt>
                  <dd>{numeral(tunnel.tunneled_bytes).format("0,0b")}</dd>
                </dl>
              </div>
            </div>
          </div>
        </div>

        <div className="row mt-3">
          <div className="col-md-12">
            <div className="card">
              <div className="card-body">
                <CardTitleWithControls title="Underlying TCP Session" />
              </div>
            </div>
          </div>
        </div>

      </React.Fragment>
  )

}