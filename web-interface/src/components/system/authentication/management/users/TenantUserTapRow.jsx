import React from "react";

function TenantUserTapRow(props) {

  const tapPermissions = props.tapPermissions;
  const allowAccessAllTaps = props.allowAccessAllTaps;

  const onChange = props.onChange;

  const tap = props.tap;

  return (
      <React.Fragment>
        <tr>
          <td style={{textAlign: "center"}}>
            <input type="checkbox"
                   checked={!allowAccessAllTaps && tapPermissions.includes(tap.uuid)}
                   disabled={allowAccessAllTaps}
                   onChange={(e) => props.onChange(e, tap.uuid) } />
          </td>
          <td className={allowAccessAllTaps ? "text-muted" : null}>{tap.name}</td>
        </tr>
      </React.Fragment>
  )

}

export default TenantUserTapRow;