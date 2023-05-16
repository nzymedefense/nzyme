import React from "react";

function TenantUserTapRow(props) {

  const tap = props.tap;
  const active = props.active;

  return (
      <React.Fragment>
        <tr>
          <td style={{textAlign: "center"}}>
            <input type="checkbox" checked={active} />
          </td>
          <td>{tap.name}</td>
        </tr>
      </React.Fragment>
  )

}

export default TenantUserTapRow;