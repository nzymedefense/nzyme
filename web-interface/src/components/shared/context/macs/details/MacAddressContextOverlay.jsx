import React, {useEffect, useState} from "react";
import ContextService from "../../../../../services/ContextService";
import {notify} from "react-notify-toast";
import ApiRoutes from "../../../../../util/ApiRoutes";
import WithPermission from "../../../../misc/WithPermission";
import AssetImage from "../../../../misc/AssetImage";
import {truncate} from "../../../../../util/Tools";
import ContextOwnerInformation from "../../../../context/ContextOwnerInformation";

const contextService = new ContextService();

function MacAddressContextOverlay(props) {

  const address = props.address;

  const [ctx, setCtx] = useState(null);
  const [is404, setIs404] = useState(false);

  useEffect(() => {
    contextService.findMacAddressContext(address, setCtx, (error) => {
      // Error.
      if (error.response) {
        if (error.response.status === 404) {
          setIs404(true);
        } else {
          notify.show('Could not load MAC address context. (HTTP ' + error.response.status + ')', 'error')
        }
      } else {
        notify.show('Could not load MAC address context. No response.', 'error')
      }
    });
  }, []);

  const contextType = (type) => {
    switch (type) {
      case "DOT11_CLIENT":
        return <span>802.11/WiFi Client</span>
      case "DOT11_AP":
        return <span>802.11/WiFi Access Point</span>
      case "DOT11_MIXED":
        return <span>802.11/WiFi Mixed/Inconclusive</span>
      case "UNKNOWN":
      default:
        return <span>Unknown</span>
    }
  }

  const monitored = (type, network) => {
    if (type === "DOT11_AP") {
      if (network) {
        return (
            <React.Fragment>
              Yes ({truncate(network.name, 22, false)}){' '}

              <WithPermission permission="dot11_monitoring_manage">
                <a href={ApiRoutes.DOT11.MONITORING.SSID_DETAILS(network.uuid)} title="Open Monitored Network Details">
                  <i className="fa-solid fa-link"></i>
                </a>
              </WithPermission>
            </React.Fragment>
        )
      } else {
        return "No"
      }
    } else {
      return "n/a"
    }
  }

  const typeDetailsLink = (type, mac) => {
    let href;
    switch (type) {
      case "DOT11_CLIENT":
        href = ApiRoutes.DOT11.CLIENTS.DETAILS(mac);
        break;
      case "DOT11_AP":
        href = ApiRoutes.DOT11.NETWORKS.BSSID(mac);
        break;
      default:
        return null;
    }

    return <a href={href} className="btn btn-sm btn-outline-secondary">Device Details</a>
  }

  if (!ctx) {
    if (is404) {
      return (
          <React.Fragment>
            <h6><i className="fa-regular fa-address-card" /> {address}</h6>

            <p className="context-description">
              This MAC address has no context.
            </p>

            <WithPermission permission="mac_aliases_manage">
              <a href={ApiRoutes.CONTEXT.MAC_ADDRESSES.CREATE + "?address=" + encodeURIComponent(address)}
                 className="btn btn-sm btn-primary context-overlay-add-context">
                Add Context
              </a>
            </WithPermission>
          </React.Fragment>
      )
    } else {
      return <React.Fragment>
        <AssetImage filename="loading-miller-notext.png"
                    className="loading-miller"
                    alt="loading ..." />

        <AssetImage filename="loading-miller_layer2-notext.png"
                    className="loading-miller loading-miller-layer2"
                    alt="loading ..." />
      </React.Fragment>
    }
  }

  return (
      <React.Fragment>
        <h6>
          <i className="sidebar-icon fa-regular fa-address-card" /> {address}{' '}
          <span className="context-name">{ctx.context.name}</span>
        </h6>

        <p className="context-description">
          <i className="fa-solid fa-angle-right"></i> {ctx.context.description}
        </p>

        <dl>
          <dt>Device Type:</dt>
          <dd>{contextType(ctx.context_type)}</dd>
          <dt>Is Monitored:</dt>
          <dd>{monitored(ctx.context_type, ctx.serves_dot11_monitored_network)}</dd>
          <dt>Has Notes:</dt>
          <dd>{ctx.context.notes ? "Yes" : "No"}</dd>
        </dl>

        <a href={ApiRoutes.CONTEXT.MAC_ADDRESSES.SHOW(ctx.context.uuid)} className="btn btn-sm btn-outline-primary">
          Context Details
        </a>{' '}
        {typeDetailsLink(ctx.context_type, address)}{' '}

        <ContextOwnerInformation context={ctx.context} />
      </React.Fragment>
  )

}

export default MacAddressContextOverlay;