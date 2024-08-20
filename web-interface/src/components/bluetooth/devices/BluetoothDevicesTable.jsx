import React, {useContext} from 'react';
import moment from "moment";
import LoadingSpinner from "../../misc/LoadingSpinner";
import Paginator from "../../misc/Paginator";
import MacAddress from "../../shared/context/macs/MacAddress";
import SignalStrength from "../../dot11/util/SignalStrength";
import {TapContext} from "../../../App";
import GroupedParameterList from "../../shared/GroupedParameterList";
import numeral from "numeral";
import BluetoothMacAddress from "../../shared/context/macs/BluetoothMacAddress";

export default function BluetoothDevicesTable(props) {

  const devices = props.devices;
  const page = props.page;
  const perPage = props.perPage;
  const setPage = props.setPage;

  const tapContext = useContext(TapContext);
  const selectedTaps = tapContext.taps;

  const transformTransport = (transport) => {
    if (transport === "bredr") {
      return <span title="Bluetooth Classic">Classic</span>;
    }

    if (transport === "le") {
      return <span title="Bluetooth Low Energy">LE</span>;
    }

    return transport;
  }

  const transformTag = (tag) => {
    switch (tag) {
      case "apple_find_my_paired": return "Apple \"Find My\" (Paired)";
      case "apple_find_my_unpaired": return "Apple \"Find My\" (Unpaired)";
      default: return tag;
    }
  }

  if (!devices) {
    return <LoadingSpinner />
  }

  if (devices.count === 0) {
    return (
        <div className="alert alert-info mb-2">
          No Bluetooth devices recorded in selected time frame.
        </div>
    )
  }

  return (
      <React.Fragment>
        <table className="table table-sm table-hover table-striped">
          <thead>
          <tr>
            <th>Address</th>
            <th>OUI</th>
            <th>Manufacturer</th>
            <th>Signal Strength</th>
            <th>Type</th>
            <th>Transport</th>
            <th>Name</th>
            <th>Class</th>
            <th>Last Seen</th>
          </tr>
          </thead>
          <tbody>
          {devices.devices.map((d, i) => {
            return (
                <tr key={i}>
                  <td>
                    <BluetoothMacAddress addressWithContext={d.mac} href="#"/>
                  </td>
                  <td>{d.mac.oui ? d.mac.oui : <span className="text-muted">Unknown</span>}</td>
                  <td><GroupedParameterList list={d.companies}/></td>
                  <td><SignalStrength strength={d.average_rssi} selectedTapCount={selectedTaps.length}/></td>
                  <td><GroupedParameterList list={d.tags} valueTransform={transformTag} /></td>
                  <td><GroupedParameterList list={d.transports} valueTransform={transformTransport}/></td>
                  <td><GroupedParameterList list={d.names}/></td>
                  <td><GroupedParameterList list={d.device_classes}/></td>
                  <td>{moment(d.last_seen).fromNow()}</td>
                </tr>
            )
          })}
          </tbody>
        </table>

        <Paginator itemCount={devices.count} perPage={perPage} setPage={setPage} page={page} />
      </React.Fragment>
  )

}