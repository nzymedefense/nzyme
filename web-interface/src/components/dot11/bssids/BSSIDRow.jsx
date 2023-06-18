import React from "react";
import moment from "moment";
import SSIDsList from "../util/SSIDsList";
import SignalStrength from "../util/SignalStrength";

function BSSIDRow(props) {
  const bssid = props.bssid;

  return (
      <tr>
        <td>{bssid.bssid}</td>
        <td><SignalStrength strength={bssid.signal_strength_average} /></td>
        <td><SSIDsList ssids={bssid.advertised_ssid_names} /></td>
        <td title={moment(bssid.last_seen).format()}>
          {moment(bssid.last_seen).fromNow()}
        </td>
      </tr>
  )

}

export default BSSIDRow;