import React, {useEffect, useState} from "react";
import LoadingSpinner from "../../../../misc/LoadingSpinner";
import ContextService from "../../../../../services/ContextService";
import {notify} from "react-notify-toast";
import ApiRoutes from "../../../../../util/ApiRoutes";
import WithPermission from "../../../../misc/WithPermission";
import AssetImage from "../../../../misc/AssetImage";

const contextService = new ContextService();

function MacAddressContextOverlay(props) {

  const address = props.address;

  const [context, setContext] = useState(null);
  const [is404, setIs404] = useState(false);

  useEffect(() => {
    contextService.findMacAddressContext(address, setContext, (error) => {
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

  if (!context) {
    if (is404) {
      return (
          <React.Fragment>
            <h6><i className="fa-regular fa-address-card" /> {address}</h6>

            <p>This MAC address has no context.</p>

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
          <span className="context-name">{context.name}</span>
        </h6>

        <p>{context.description}</p>
      </React.Fragment>
  )

}

export default MacAddressContextOverlay;