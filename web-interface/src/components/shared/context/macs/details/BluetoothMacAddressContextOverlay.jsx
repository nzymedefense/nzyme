import React, {useEffect, useState} from "react";
import ContextService from "../../../../../services/ContextService";
import ApiRoutes from "../../../../../util/ApiRoutes";
import WithPermission from "../../../../misc/WithPermission";
import AssetImage from "../../../../misc/AssetImage";
import ContextOwnerInformation from "../../../../context/ContextOwnerInformation";

const contextService = new ContextService();

function BluetoothMacAddressContextOverlay(props) {

  const address = props.address;
  const isRandomized = props.isRandomized;

  const [ctx, setCtx] = useState(null);

  useEffect(() => {
    setCtx(null);
    contextService.findMacAddressContext(address, setCtx);
  }, [address]);

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

          <dl style={{marginBottom: 30}}>
            <dt>Device Type:</dt>
            <dd>Bluetooth Device</dd>
            <dt>Has Notes:</dt>
            <dd>No</dd>
            <dt>Is Randomized:</dt>
            <dd>{isRandomized === null ? "n/a" : (isRandomized ? "Yes" : "No")}</dd>
          </dl>

          <div className="context-overlay-no-context-controls">
            <WithPermission permission="mac_aliases_manage">
              <a href={ApiRoutes.CONTEXT.MAC_ADDRESSES.CREATE + "?address=" + encodeURIComponent(address)}
                 className="btn btn-sm btn-outline-primary">
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
          <i className="fa-solid fa-angle-right"></i> {ctx.context.description}
        </p>

        <dl style={{marginBottom: 30}}>
          <dt>Device Type:</dt>
          <dd>Bluetooth Device</dd>
          <dt>Has Notes:</dt>
          <dd>{ctx.context.notes ? "Yes" : "No"}</dd>
          <dt>Is Randomized:</dt>
          <dd>{isRandomized === null ? "n/a" : (isRandomized ? "Yes" : "No")}</dd>
        </dl>

        <a href={ApiRoutes.CONTEXT.MAC_ADDRESSES.SHOW(ctx.context.uuid, ctx.context.organization_id, ctx.context.tenant_id)}
           className="btn btn-sm btn-outline-primary">
          Context Details
        </a>{' '}

        <ContextOwnerInformation context={ctx.context} />
      </React.Fragment>
  )

}

export default BluetoothMacAddressContextOverlay;