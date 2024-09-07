import React from "react";
import WithPermission from "../../../../misc/WithPermission";
import ApiRoutes from "../../../../../util/ApiRoutes";

export default function MacAddressContextLine(props) {

  const address = props.address;
  const ctx = props.context;

  if (!ctx) {
    return (
        <React.Fragment>
          No Context Configured{' '}
          <WithPermission permission="mac_aliases_manage">
            (<a href={ApiRoutes.CONTEXT.MAC_ADDRESSES.CREATE + "?address=" + encodeURIComponent(address)}>
              Add Context
            </a>)
          </WithPermission>
        </React.Fragment>
    )
  }

  return (
      <React.Fragment>
        {ctx.name ? <span className="context-name">{ctx.name}</span> : <span className="text-muted">No Name</span> }
        {' '}
        {ctx.description ? <span>({ctx.description})</span> : null }

      </React.Fragment>
  )

}