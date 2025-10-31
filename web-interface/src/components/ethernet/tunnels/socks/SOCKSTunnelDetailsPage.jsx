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
import moment from "moment/moment";
import {formatDurationMs} from "../../../../util/Tools";
import SOCKSTunnelDestination from "./SOCKSTunnelDestination";
import {disableTapSelector, enableTapSelector} from "../../../misc/TapSelector";
import InternalAddressOnlyWrapper from "../../shared/InternalAddressOnlyWrapper";
import EthernetMacAddress from "../../../shared/context/macs/EthernetMacAddress";
import L4Address from "../../shared/L4Address";
import TCPSessionLink from "../../shared/TCPSessionLink";

const socksService = new SocksService();

export default function SOCKSTunnelDetailsPage() {

  const { tunnelId } = useParams();

  const tapContext = useContext(TapContext);
  const selectedTaps = tapContext.taps;

  const [organizationId, tenantId] = useSelectedTenant();

  const [tunnel, setTunnel] = useState(null);

  useEffect(() => {
    enableTapSelector(tapContext);

    return () => {
      disableTapSelector(tapContext);
    }
  }, [tapContext]);

  useEffect(() => {
    setTunnel(null);
    socksService.findTunnel(tunnelId, organizationId, tenantId, selectedTaps, setTunnel);
  }, [tunnelId, organizationId, tenantId])

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
                <CardTitleWithControls title="Tunnel Source &amp; Destination" />

                <dl className="mb-0">
                  <dt>Client Address</dt>
                  <dd><L4Address address={tunnel.client} /></dd>
                  <dt>Client Asset</dt>
                  <dd>
                    <InternalAddressOnlyWrapper
                        address={tunnel.client}
                        inner={<EthernetMacAddress addressWithContext={tunnel.client.mac}
                                                   withAssetLink withAssetName />} />
                  </dd>
                  <dt>Server Address</dt>
                  <dd><SOCKSTunnelDestination tunnel={tunnel} /></dd>
                  <dt>Server Asset</dt>
                  <dd>
                    <InternalAddressOnlyWrapper
                        address={tunnel.socks_server}
                        inner={<EthernetMacAddress addressWithContext={tunnel.socks_server.mac}
                                                   withAssetLink withAssetName />} />
                  </dd>
                  <dt>Tunnel Destination</dt>
                  <dt><SOCKSTunnelDestination tunnel={tunnel} /></dt>
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
                  <dt>Duration</dt>
                  <dd>{formatDurationMs(tunnel.duration_ms)}</dd>
                  <dt>Established at</dt>
                  <dd>{moment(tunnel.established_at).format()}</dd>
                  <dt>Terminated at</dt>
                  <dd>
                    {tunnel.terminated_at ? moment(tunnel.terminated_at).format() :
                        <span className="text-muted">n/a</span>}
                  </dd>
                  <dt>Underlying TCP Session</dt>
                  <dd><TCPSessionLink sessionId={tunnel.tcp_session_key} /></dd>
                </dl>
              </div>
            </div>
          </div>
        </div>

      </React.Fragment>
  )

}