import React, {useEffect, useState} from "react";
import AssetImage from "../../../../misc/AssetImage";
import WithPermission from "../../../../misc/WithPermission";
import ApiRoutes from "../../../../../util/ApiRoutes";
import ContextService from "../../../../../services/ContextService";
import {truncate} from "../../../../../util/Tools";
import FirstContextIpAddress from "./FirstContextIpAddress";
import FirstContextHostname from "./FirstContextHostname";
import useSelectedTenant from "../../../../system/tenantselector/useSelectedTenant";

const contextService = new ContextService();

export default function EthernetMacAddressContextOverlay(props) {

  const [organizationId, tenantId] = useSelectedTenant();

  const address = props.address;
  const oui = props.oui;

  // Optional.
  const assetId = props.assetId;

  const [ctx, setCtx] = useState(null);

  useEffect(() => {
    setCtx(null);
    contextService.findMacAddressContext(address, organizationId, tenantId, setCtx);
  }, [address]);

  const assetLink = () => {
    if (!assetId) {
      return null;
    }

    return (
        <React.Fragment>
          <a href={ApiRoutes.ETHERNET.ASSETS.DETAILS(assetId)}
             className="btn btn-sm btn-outline-primary">
            Open Asset Page
          </a>{' '}
        </React.Fragment>
    )
  }

  if (!ctx) {
    return (
        <React.Fragment>
          <AssetImage filename="loading-miller-notext.png"
                      className="loading-miller"
                      alt="loading ..." />

          <AssetImage filename="loading-miller_layer2-notext.png"
                      className="loading-miller loading-miller-layer2"
                      alt="loading ..." />
        </React.Fragment>
    )
  }

  if (!ctx.context) {
    return (
        <React.Fragment>
          <h6><i className="fa-regular fa-address-card" /> {address}</h6>

          <p className="context-description">
            <i className="fa-solid fa-circle-info"></i> This MAC address has no context.
          </p>

          <dl style={{marginBottom: 90}}>
            <dt>Device Type:</dt>
            <dd>Ethernet</dd>
            <dt>OUI:</dt>
            <dd>{oui ? truncate(oui, 20) : <span className="text-muted">Unknown</span>}</dd>
            <dt>Has Notes:</dt>
            <dd>No</dd>
          </dl>

          <div className="context-overlay-no-context-controls">
            {assetLink()}

            <WithPermission permission="mac_context_manage">
              <a href={ApiRoutes.CONTEXT.MAC_ADDRESSES.CREATE + "?address=" + encodeURIComponent(address)}
                 className="btn btn-sm btn-outline-secondary">
                Add Context
              </a>{' '}
            </WithPermission>
          </div>
        </React.Fragment>
    )
  }

  return (
      <React.Fragment>
        <h6>
          <i className="fa-regular fa-address-card" /> {address}{' '}
          <span className="context-name">{ctx.context.name}</span>
        </h6>

        <p className="context-description">
          <i className="fa-solid fa-angle-right"></i>{' '}
          {ctx.context.description && ctx.context.description.trim().length > 0
              ? ctx.context.description : "No Description"}
        </p>

        <dl style={{marginBottom: 47}}>
          <dt>Device Type:</dt>
          <dd>Ethernet Device</dd>
          <dt>OUI:</dt>
          <dd>{oui ? truncate(oui, 20) : <span className="text-muted">Unknown</span>}</dd>
          <dt>IP Address</dt>
          <dd><FirstContextIpAddress addresses={ctx.context.transparent_ip_addresses}/></dd>
          <dt>Hostname</dt>
          <dd><FirstContextHostname hostnames={ctx.context.transparent_hostnames}/></dd>
          <dt>Has Notes:</dt>
          <dd>{ctx.context.notes ? "Yes" : "No"}</dd>
        </dl>

        {assetLink()}

        <a href={ApiRoutes.CONTEXT.MAC_ADDRESSES.SHOW(ctx.context.uuid, ctx.context.organization_id, ctx.context.tenant_id)}
           className="btn btn-sm btn-outline-primary">
          Context Details
        </a>{' '}
      </React.Fragment>
  )

}