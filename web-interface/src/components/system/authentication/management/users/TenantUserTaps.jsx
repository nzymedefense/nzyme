import React from "react";
import TenantUserTapRow from "./TenantUserTapRow";

function TenantUserTaps(props) {

  const taps = props.taps;
  const user = props.user;

  return (
      <React.Fragment>
        <table className="table table-sm table-hover table-striped">
          <thead>
          <tr>
            <th style={{width: 35}}>&nbsp;</th>
            <th>Tap Name</th>
          </tr>
          </thead>
          <tbody>
          <tr>
            <td style={{textAlign: "center"}}><input type="checkbox" checked={true} /></td>
            <td><strong>Allow access to all taps</strong></td>
          </tr>
          {Object.values(taps.taps).map(function (tap, i) {
            return <TenantUserTapRow key={"tapperm-" + i} tap={tap} active={false} />
          })}
          </tbody>
        </table>
      </React.Fragment>
  )

}

export default TenantUserTaps;