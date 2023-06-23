import React, {useContext, useState} from "react";
import moment from "moment";
import SSIDsList from "../util/SSIDsList";
import SignalStrength from "../util/SignalStrength";
import Dot11Service from "../../../services/Dot11Service";
import {TapContext} from "../../../App";
import BSSIDDetailsRows from "./BSSIDDetailsRows";

const dot11Service = new Dot11Service();

function BSSIDRow(props) {
  const tapContext = useContext(TapContext);

  const bssid = props.bssid;
  const minutes = props.minutes;

  const [ssids, setSSIDs] = useState(null);
  const [ssidsLoading, setSSIDsLoading] = useState(false);

  const selectedTaps = tapContext.taps;

  const onExpandClick = function(bssid) {
    if (ssidsLoading) {
      // Don't do anything if already loading.
      return;
    }

    if (ssids === null) {
      setSSIDsLoading(true);
      dot11Service.findSSIDsOfBSSID(bssid, minutes, selectedTaps, function(ssids) {
        setSSIDs(ssids);
        setSSIDsLoading(false);
      });
    } else {
      setSSIDs(null);
    }
  }

  return (
      <React.Fragment>
        <tr>
          <td>
            <a href="#" onClick={() => onExpandClick(bssid.bssid)}>{bssid.bssid}</a>
          </td>
          <td><SignalStrength strength={bssid.signal_strength_average} /></td>
          <td>
            { bssid.has_hidden_ssid_advertisements ? <span className="hidden-ssid">&lt;hidden&gt;</span> : null }{ bssid.has_hidden_ssid_advertisements && bssid.advertised_ssid_names.length > 0 ? ", " : null }
            <SSIDsList ssids={bssid.advertised_ssid_names} />
          </td>
          <td>{bssid.security_protocols.length === 0 ? "None" : bssid.security_protocols.join(",")}</td>
          <td>{bssid.oui ? bssid.oui : "Unknown"}</td>
          <td title={moment(bssid.last_seen).format()}>
            {moment(bssid.last_seen).fromNow()}
          </td>
        </tr>
        <BSSIDDetailsRows ssids={ssids} loading={ssidsLoading} />
      </React.Fragment>
  )

}

export default BSSIDRow;